#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
ws_client.py

Device-side WebSocket client for meeting-room terminals.
Responsibilities:
- Connect to server WebSocket endpoint and authenticate using deviceUuid + secretKey
- Respond to server's {"type":"requestSensor"} by fetching local /status and sending
  {"type":"sensorData","deviceUuid":...,"payload":...,"timestamp":...}
- Optional: periodically push sensor data
- Auto-reconnect, logging and basic error handling

Usage:
  1) Install dependencies: pip3 install websocket-client requests
  2) Place this file on the device and ensure `secret_key.txt` exists (created by your register flow)
  3) Configure constants below if needed (WS_URL, STATUS_URL)
  4) Run: python3 script/ws_client.py

You may run this as a systemd service (example provided in README section below).
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

# -------- Configuration (edit if needed) --------
# WebSocket server endpoint (use ws:// or wss:// depending on your server)
WS_URL = os.environ.get('WS_URL', 'ws://192.168.1.103:8088/ws/device')
# Local sensor status HTTP endpoint provided by the collector app
STATUS_URL = os.environ.get('STATUS_URL', 'http://127.0.0.1:5001/status')
# Path to the secret key file (written by your registration flow)
SECRET_KEY_FILE = os.environ.get('SECRET_KEY_FILE', 'secret_key.txt')
# Reconnect delay in seconds
RECONNECT_DELAY = 5
# If set to a positive integer, periodically (seconds) push sensor data even without requestSensor
PERIODIC_PUSH_INTERVAL = int(os.environ.get('PERIODIC_PUSH_INTERVAL', '0'))
# Optional: send periodic heartbeat over WS every N seconds (0=disabled)
WS_HEARTBEAT_INTERVAL = int(os.environ.get('WS_HEARTBEAT_INTERVAL', '0'))

# -------- Logging --------
logging.basicConfig(level=logging.INFO, format='[ws_client] %(asctime)s %(levelname)s: %(message)s')
logger = logging.getLogger('ws_client')

# -------- Utility functions --------

def load_secret_key():
    try:
        if os.path.exists(SECRET_KEY_FILE):
            with open(SECRET_KEY_FILE, 'r') as f:
                return f.read().strip()
    except Exception as e:
        logger.error('Failed to load secret key: %s', e)
    return None


def get_mac_address():
    # Fallback method to derive MAC address as device UUID (mirrors user's script)
    mac = uuid.UUID(int=uuid.getnode()).hex[-12:]
    return ':'.join([mac[e:e+2] for e in range(0, 12, 2)])


# -------- WS client callbacks and helpers --------
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
            logger.warning('Failed to fetch local status: %s', e)
            return None

    def _make_sensor_message(self, payload):
        return {
            "type": "sensorData",
            "deviceUuid": self.device_uuid,
            "payload": payload,
            "timestamp": datetime.utcnow().isoformat() + 'Z'
        }

    def on_open(self, ws):
        logger.info('WebSocket opened, sending auth')
        auth = self._build_auth_payload()
        try:
            ws.send(json.dumps(auth))
        except Exception as e:
            logger.error('Failed to send auth: %s', e)

        # optional WS heartbeat
        if WS_HEARTBEAT_INTERVAL > 0:
            def hb_loop():
                while not self._stop.wait(WS_HEARTBEAT_INTERVAL):
                    try:
                        hb = {"type": "heartbeat", "deviceUuid": self.device_uuid}
                        ws.send(json.dumps(hb))
                    except Exception as ex:
                        logger.warning('WS heartbeat send failed: %s', ex)
                        break
            threading.Thread(target=hb_loop, daemon=True).start()

        # optional periodic push
        if PERIODIC_PUSH_INTERVAL > 0:
            def push_loop():
                while not self._stop.wait(PERIODIC_PUSH_INTERVAL):
                    payload = self._fetch_local_status()
                    if payload is None:
                        continue
                    msg = self._make_sensor_message(payload)
                    try:
                        ws.send(json.dumps(msg))
                        logger.info('Periodic sensorData sent')
                    except Exception as ex:
                        logger.warning('Failed to send periodic sensorData: %s', ex)
                        break
            threading.Thread(target=push_loop, daemon=True).start()

    def on_message(self, ws, message):
        try:
            msg = json.loads(message)
        except Exception as e:
            logger.warning('Invalid JSON from server: %s', e)
            return

        typ = msg.get('type')
        if typ == 'requestSensor':
            logger.info('Received requestSensor from server')
            payload = self._fetch_local_status()
            if payload is None:
                logger.warning('Local status not available, skipping sensorData send')
                return
            out = self._make_sensor_message(payload)
            try:
                ws.send(json.dumps(out))
                logger.info('Sent sensorData in response to requestSensor')
            except Exception as e:
                logger.error('Failed to send sensorData: %s', e)
        elif typ == 'ping':
            # optional custom ping handling
            logger.debug('Server ping: %s', msg)
        else:
            logger.debug('Unhandled server message: %s', msg)

    def on_close(self, ws, close_status_code, close_msg):
        logger.warning('WebSocket closed: %s %s', close_status_code, close_msg)

    def on_error(self, ws, error):
        logger.error('WebSocket error: %s', error)

    def start(self):
        if not self.secret_key:
            logger.error('secret_key not found at %s. Obtain via register flow before running ws_client.', SECRET_KEY_FILE)
            return False

        self._stop.clear()
        while not self._stop.is_set():
            try:
                logger.info('Connecting to %s', self.ws_url)
                self.ws_app = WebSocketApp(self.ws_url,
                                           on_open=self.on_open,
                                           on_message=self.on_message,
                                           on_close=self.on_close,
                                           on_error=self.on_error)
                # run_forever will block until closed
                self.ws_app.run_forever(ping_interval=20, ping_timeout=10)
            except Exception as e:
                logger.error('WS run_forever terminated with exception: %s', e)
            if self._stop.wait(RECONNECT_DELAY):
                break
            logger.info('Reconnecting in %ds...', RECONNECT_DELAY)
        return True

    def stop(self):
        self._stop.set()
        try:
            if self.ws_app:
                self.ws_app.close()
        except Exception:
            pass


# -------- Entrypoint --------
def main():
    client = DeviceWSClient(WS_URL, STATUS_URL)
    try:
        client.start()
    except KeyboardInterrupt:
        logger.info('Interrupted, stopping client')
        client.stop()


if __name__ == '__main__':
    main()


# -------- README & systemd example --------
# systemd unit example (save as /etc/systemd/system/room-ws.service):
# [Unit]
# Description=Meeting room WS client
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
# After deploying the file on the device:
# sudo pip3 install websocket-client requests
# sudo systemctl daemon-reload
# sudo systemctl enable room-ws.service
# sudo systemctl start room-ws.service
# sudo journalctl -u room-ws.service -f

