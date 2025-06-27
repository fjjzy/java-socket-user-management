#!/bin/bash

# Java Socket 项目编译脚本
# 用于编译服务器端和客户端代码

echo "=================================="
echo "Java Socket 项目编译脚本"
echo "=================================="

# 检查Java环境
if ! command -v javac &> /dev/null; then
    echo "错误: 未找到javac命令，请确保已安装Java开发环境"
    exit 1
fi

echo "Java版本信息:"
java -version
echo ""

# 创建编译输出目录
echo "创建编译输出目录..."
mkdir -p build

# 编译服务器端代码
echo "编译服务器端代码..."
javac -cp "lib/sqlite-jdbc-3.42.0.0.jar" -d build src/server/*.java

if [ $? -eq 0 ]; then
    echo "✓ 服务器端编译成功"
else
    echo "✗ 服务器端编译失败"
    exit 1
fi

# 编译客户端代码
echo "编译客户端代码..."
javac -d build -cp "lib/sqlite-jdbc-3.42.0.0.jar" src/client/*.java

if [ $? -eq 0 ]; then
    echo "✓ 客户端编译成功"
else
    echo "✗ 客户端编译失败"
    exit 1
fi

echo ""
echo "=================================="
echo "编译完成！"
echo "=================================="
echo "运行服务器: ./run_server.sh"
echo "运行客户端: ./run_client.sh"
echo "=================================="