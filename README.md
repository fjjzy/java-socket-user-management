# Java Socket 前后端项目

一个简单的Java前后端应用，使用Socket进行通信，SQLite作为数据库，具有增删查改功能，不使用任何框架，供以参考，有问题可以提交issues。
这是我大二下小学期的作业。
特别鸣谢小学期王全新老师。

## 项目结构

```
java-socket-user-management/
├── src/
│   ├── client/
│   │   ├── ClientGUI.java          # 前端GUI界面
│   │   └── SocketClient.java       # Socket客户端
│   └── server/
│       ├── SocketServer.java       # Socket服务器
│       ├── DatabaseManager.java    # 数据库管理
│       └── User.java              # 用户实体类
├── lib/
│   └── sqlite-jdbc-3.42.0.0.jar   # SQLite数据库驱动
├── database/
│   └── app.db                      # SQLite数据库文件
└── README.md
```

## 功能特性

- 用户注册和登录
- 用户信息管理
- Socket通信
- SQLite数据库存储
- 简洁的Swing GUI界面

## 运行要求

- Java 8 或更高版本
- SQLite JDBC 驱动

## 编译和运行

### 1. 编译项目

```bash
# 编译服务器端
javac -cp "lib/sqlite-jdbc-3.42.0.0.jar" -d . src/server/*.java

# 编译客户端
javac -d . src/client/*.java
```

### 2. 运行服务器

```bash
java -cp ".:lib/sqlite-jdbc-3.42.0.0.jar" server.SocketServer
```

### 3. 运行客户端

```bash
java client.ClientGUI
```

## 通信协议

客户端和服务器使用JSON格式进行通信：

```json
{
  "action": "login",
  "data": {
    "username": "user1",
    "password": "pass123"
  }
}
```

支持的操作：
- `register`: 用户注册
- `login`: 用户登录
- `getUserList`: 获取用户列表、本地筛选搜索用户、修改用户信息、删除用户

## 数据库结构

用户表 (users):
- id (INTEGER PRIMARY KEY)
- username (TEXT UNIQUE)
- password (TEXT)
- email (TEXT)
- created_at (TIMESTAMP)

## 注意事项

- 服务器默认监听端口：8888
- 数据库文件会自动创建在 `database/app.db`
- 密码存储使用SHA2加密+随机加盐
