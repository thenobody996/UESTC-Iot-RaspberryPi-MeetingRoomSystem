#!/bin/bash

JAP_PATH="/var/www/app-jars//opt/webserver/zongshe-0.0.1-SNAPSHOT.jar"

# 查找占用 8088 端口的进程
pid=$(lsof -t -i:8088)

if [ -n "$pid" ]; then
  echo "发现占用8088端口的进程 PID=$pid，正在结束..."
  sudo kill -9 $pid
  echo "进程 $pid 已结束"
else
  echo "未发现占用8088端口的进程"
fi

# 启动新的 Java 程序
echo "正在启动新的程序: $JAP_PATH"
nohup java -jar $JAP_PATH > /var/www/logs/web.log 2>&1 &
echo "新程序已启动，日志输出到 web.log"
