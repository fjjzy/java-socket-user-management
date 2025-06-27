# Java Socket 前后端项目 Demo

一个简单的Java前后端应用Demo，使用Socket进行通信，SQLite作为数据库，具有增删查改功能，不使用任何框架，供以参考，有问题可以提交issues。
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
│       └── User.java               # 用户实体类
├── lib/
│   └── sqlite-jdbc-3.42.0.0.jar    # SQLite数据库驱动
├── database/
│   └── app.db                      # SQLite数据库文件
└── compile.sh                      # 编译脚本
└── run_client.sh                   # 运行客户端脚本
└── run_server.sh                   # 运行服务端脚本
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

## 写给小白的我

- 显示端口/地址使用中，大概率是你上一次启动的程序没关，导致端口被占用， 关了程序就行，或者手动查找占用端口的程序。
- 以后实习别把数据库传上去了
- 一般推荐两个终端分开运行，一个运行程序，一个运行测试用例

## 写给找bug的我

- 对象null 可能是局部重复声明了，导致其他地方的对象null
- `./run_server.sh` 与 `sh ./run_server.sh`



