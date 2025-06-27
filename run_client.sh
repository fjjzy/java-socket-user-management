#!/bin/bash

# Java Socket 客户端启动脚本

echo "=================================="
echo "启动 Java Socket 客户端"
echo "=================================="

# 检查编译文件是否存在
if [ ! -d "build" ] || [ ! -f "build/client/ClientGUI.class" ]; then
    echo "错误: 未找到编译后的客户端文件"
    echo "请先运行编译脚本: ./compile.sh"
    exit 1
fi

echo "客户端配置:"
echo "- 服务器地址: localhost:8888"
echo "- 界面: Swing GUI"
echo ""
echo "启动图形界面..."
echo "=================================="
echo ""

# 启动客户端
cd build
java client.ClientGUI