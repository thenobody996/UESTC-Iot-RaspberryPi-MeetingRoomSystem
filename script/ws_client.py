#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ws_client.py

设备端 WebSocket 客户端（用于会议室终端）。
职责：
- 连接到服务器的 WebSocket 接口，并使用 deviceUuid + secretKey 进行身份认证
- 响应服务器的 {"type":"requestSensor"} 请求：读取本地 /status 并发送
  {"type":"sensorData","deviceUuid":...,"payload":...,"timestamp":...}
- 可选：周期性地主动推送传感器数据
- 自动重连、日志记录与基本的错误处理

使用：
  1) 安装依赖：pip3 install websocket-client requests
  2) 将此文件放到设备上，并确保 `secret_key.txt` 已由注册流程写入
  3) 如需修改，调整下方常量（WS_URL, STATUS_URL 等）
  4) 运行：python3 script/ws_client.py

可将此程序作为 systemd 服务运行（文件末尾给出示例）。
"""

import json
import time
import threading
import requests
import os
import uuid
import logging
from websocket import WebSocketApp
from datetime import datetime

# -------- 配置（如需修改） --------
# WebSocket 服务器地址（根据实际情况使用 ws:// 或 wss://）
WS_URL = os.environ.get('WS_URL', 'ws://192.168.1.103:8088/ws/device')
# 本地传感器状态 HTTP 接口，由 collector 应用提供
STATUS_URL = os.environ.get('STATUS_URL', 'http://127.0.0.1:5001/status')
# 秘钥文件路径（由注册流程写入）
SECRET_KEY_FILE = os.environ.get('SECRET_KEY_FILE', 'secret_key.txt')
# 重连延迟（秒）
RECONNECT_DELAY = 5
# 如果设置为正整数，周期性（秒）推送传感器数据，即使没有 requestSensor 请求
PERIODIC_PUSH_INTERVAL = int(os.environ.get('PERIODIC_PUSH_INTERVAL', '0'))
# 可选：每 N 秒通过 WS 发送心跳（0=禁用）
WS_HEARTBEAT_INTERVAL = int(os.environ.get('WS_HEARTBEAT_INTERVAL', '0'))

# -------- 日志配置 --------
logging.basicConfig(level=logging.INFO, format='[ws_client] %(asctime)s %(levelname)s: %(message)s')
logger = logging.getLogger('ws_client')

# -------- 工具函数 --------

def load_secret_key():
    try:
        if os.path.exists(SECRET_KEY_FILE):
            with open(SECRET_KEY_FILE, 'r') as f:
                return f.read().strip()
    except Exception as e:
        logger.error('加载秘钥失败: %s', e)
    return None


def get_mac_address():
    # 备用方法：通过 MAC 地址派生设备 UUID（镜像用户的脚本）
    mac = uuid.UUID(int=uuid.getnode()).hex[-12:]
    return ':'.join([mac[e:e+2] for e in range(0, 12, 2)])


# -------- WebSocket 回调与辅助函数 --------
class DeviceWSClient:
    def __init__(self, ws_url, status_url):
        self.ws_url = ws_url
        self.status_url = status_url
        self.device_uuid = get_mac_address()
        self.secret_key = load_secret_key()
        self.ws_app = None
        self.ws_thread = None
        self._stop = threading.Event()
        self._ws_lock = threading.Lock()

    def _build_auth_payload(self):
        return {"type": "auth", "deviceUuid": self.device_uuid, "secretKey": self.secret_key}

    def _fetch_local_status(self):
        try:
            r = requests.get(self.status_url, timeout=3)
            r.raise_for_status()
            return r.json()
        except Exception as e:
            logger.warning('获取本地状态失败: %s', e)
            return None

    def _make_sensor_message(self, payload):
        return {
            "type": "sensorData",
            "deviceUuid": self.device_uuid,
            "payload": payload,
            "timestamp": datetime.utcnow().isoformat() + 'Z'
        }

    def on_open(self, ws):
        logger.info('WebSocket 已打开，发送身份认证')
        auth = self._build_auth_payload()
        try:
            ws.send(json.dumps(auth))
        except Exception as e:
            logger.error('发送身份认证失败: %s', e)

        # 可选的 WS 心跳
        if WS_HEARTBEAT_INTERVAL > 0:
            def hb_loop():
                while not self._stop.wait(WS_HEARTBEAT_INTERVAL):
                    try:
                        hb = {"type": "heartbeat", "deviceUuid": self.device_uuid}
                        ws.send(json.dumps(hb))
                    except Exception as ex:
                        logger.warning('WS 心跳发送失败: %s', ex)
                        break
            threading.Thread(target=hb_loop, daemon=True).start()

        # 可选的周期性推送
        if PERIODIC_PUSH_INTERVAL > 0:
            def push_loop():
                while not self._stop.wait(PERIODIC_PUSH_INTERVAL):
                    payload = self._fetch_local_status()
                    if payload is None:
                        continue
                    msg = self._make_sensor_message(payload)
                    try:
                        ws.send(json.dumps(msg))
                        logger.info('已发送周期性传感器数据')
                    except Exception as ex:
                        logger.warning('发送周期性传感器数据失败: %s', ex)
                        break
            threading.Thread(target=push_loop, daemon=True).start()

    def on_message(self, ws, message):
        try:
            msg = json.loads(message)
        except Exception as e:
            logger.warning('来自服务器的 JSON 无效: %s', e)
            return

        typ = msg.get('type')
        if typ == 'requestSensor':
            logger.info('收到服务器的 requestSensor 请求')
            payload = self._fetch_local_status()
            if payload is None:
                logger.warning('本地状态不可用，跳过传感器数据发送')
                return
            out = self._make_sensor_message(payload)
            try:
                ws.send(json.dumps(out))
                logger.info('已发送传感器数据以响应 requestSensor')
            except Exception as e:
                logger.error('发送传感器数据失败: %s', e)
        elif typ == 'ping':
            # 可选的自定义 ping 处理
            logger.debug('服务器 ping: %s', msg)
        else:
            logger.debug('未处理的服务器消息: %s', msg)

    def on_close(self, ws, close_status_code, close_msg):
        logger.warning('WebSocket 已关闭: %s %s', close_status_code, close_msg)

    def on_error(self, ws, error):
        logger.error('WebSocket 错误: %s', error)

    def start(self):
        if not self.secret_key:
            logger.error('未找到秘钥文件 %s。请先通过注册流程获取秘钥，然后再运行 ws_client。', SECRET_KEY_FILE)
            return False

        self._stop.clear()
        while not self._stop.is_set():
            try:
                logger.info('连接到 %s', self.ws_url)
                self.ws_app = WebSocketApp(self.ws_url,
                                           on_open=self.on_open,
                                           on_message=self.on_message,
                                           on_close=self.on_close,
                                           on_error=self.on_error)
                # run_forever 将阻塞直到关闭
                self.ws_app.run_forever(ping_interval=20, ping_timeout=10)
            except Exception as e:
                logger.error('WS run_forever 发生异常: %s', e)
            if self._stop.wait(RECONNECT_DELAY):
                break
            logger.info(' %ds 后重新连接...', RECONNECT_DELAY)
        return True

    def stop(self):
        self._stop.set()
        try:
            if self.ws_app:
                self.ws_app.close()
        except Exception:
            pass


# -------- 主入口 --------
def main():
    client = DeviceWSClient(WS_URL, STATUS_URL)
    try:
        client.start()
    except KeyboardInterrupt:
        logger.info('被中断，正在停止客户端')
        client.stop()


if __name__ == '__main__':
    main()


# -------- README 与 systemd 示例 --------
# systemd 单元示例（保存为 /etc/systemd/system/room-ws.service）：
# [Unit]
# Description=会议室 WebSocket 客户端
# After=network.target
#
# [Service]
# Type=simple
# User=pi
# WorkingDirectory=/home/pi/your-project-directory
# ExecStart=/usr/bin/python3 /home/pi/your-project-directory/script/ws_client.py
# Restart=always
# RestartSec=5
#
# [Install]
# WantedBy=multi-user.target
#
# 部署到设备后：
# sudo pip3 install websocket-client requests
# sudo systemctl daemon-reload
# sudo systemctl enable room-ws.service
# sudo systemctl start room-ws.service
# sudo journalctl -u room-ws.service -f

