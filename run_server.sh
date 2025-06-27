#!/bin/bash

# Java Socket 服务器启动脚本

echo "=================================="
echo "启动 Java Socket 服务器"
echo "=================================="

# 检查编译文件是否存在
if [ ! -d "build" ] || [ ! -f "build/server/SocketServer.class" ]; then
    echo "错误: 未找到编译后的服务器文件"
    echo "请先运行编译脚本: ./compile.sh"
    exit 1
fi

# 检查SQLite驱动是否存在
if [ ! -f "lib/sqlite-jdbc-3.42.0.0.jar" ]; then
    echo "错误: 未找到SQLite JDBC驱动"
    echo "请确保 lib/sqlite-jdbc-3.42.0.0.jar 文件存在"
    exit 1
fi

# 创建数据库目录
mkdir -p database

echo "服务器配置:"
echo "- 监听端口: 8888"
echo "- 数据库文件: database/app.db"
echo "- 最大连接数: 10"
echo ""
echo "按 Ctrl+C 停止服务器"
echo "=================================="
echo ""

# 启动服务器
cd build
java -cp ".:../lib/sqlite-jdbc-3.42.0.0.jar" server.SocketServer