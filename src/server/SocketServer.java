package server;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Socket服务器类
 * 负责监听客户端连接，处理客户端请求
 */
public class SocketServer {

    private static final int PORT = 8888;
    private static final int Max_CLIENTS = 10;

    private ServerSocket serverSocket;
    private DatabaseManager dbManager;
    private ExecutorService threadPool;
    private boolean isRunning = false;


    /**
     * 构造函数，初始化服务器
     */
    public SocketServer() {
        dbManager = new DatabaseManager();
        threadPool = Executors.newFixedThreadPool(Max_CLIENTS);
    }

    /**
     * 启动服务器
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;

            System.out.println("==================================");
            System.out.println("服务器启动成功！");
            System.out.println("监听端口: " + PORT);
            System.out.println("等待客户端连接...");
            System.out.println("==================================");

            //监听客户端连接
            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("新客户端连接: " + clientSocket.getInetAddress().getHostAddress());

                    // 创建一个线程来处理客户端请求
                    threadPool.submit(new ClientHandler(clientSocket, dbManager));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("接受客户端连接失败: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止服务器
     */
    public void stop() {
        isRunning = false;
        try {
           if(serverSocket != null && !serverSocket.isClosed()) {
               serverSocket.close();
           }
           threadPool.shutdown();
           dbManager.closeConnection();
           System.out.println("服务器已停止");
        } catch (IOException e) {
            System.err.println("停止服务器失败: " + e.getMessage());
        }
    }

    /**
     * 客户端处理器内部类
     * 处理单个客户端的请求
     */
    public static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DatabaseManager dbManager;
        private BufferedReader reader;
        private PrintWriter writer;

        /**
         * 构造函数
         * @param clientSocket 客户端Socket
         * @param dbManager 数据库管理器
         */
        public ClientHandler(Socket clientSocket, DatabaseManager dbManager) {
            this.clientSocket = clientSocket;
            this.dbManager = dbManager;
        }

        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);

                String ClientAddress = clientSocket.getInetAddress().getHostAddress();
                System.out.println("开始处理客户端请求: " + ClientAddress);

                // 处理客户端请求
                String request;
                while ((request = reader.readLine()) != null) {
                    System.out.println("收到请求: " + request);
                    String response = processRequest(request);
                    writer.println(response);
                    System.out.println("发送响应: " + response);
                }
            } catch (IOException e) {
                System.err.println("无法创建输入流");
            } finally {
                closeConnection();
            }
        }


        /**
         * 处理客户端请求
         * @param request 请求字符串（JSON格式）
         * @return 响应字符串（JSON格式）
         */
        private String processRequest(String request) {
            try {
                // 简单的JSON解析（实际项目中应使用JSON库）
                if (request.contains("\"action\":\"register\"")) {
                    return handleRegister(request);
                } else if (request.contains("\"action\":\"login\"")) {
                    return handleLogin(request);
                } else if (request.contains("\"action\":\"getUserList\"")) {
                    return handleGetUserList();
                } else if (request.contains("\"action\":\"deleteUser\"")) {
                    return handleDeleteUser(request);
                } else if (request.contains("\"action\":\"getUserByUsername\"")) {
                    return handleGetUserByUsername(request);
                } else if (request.contains("\"action\":\"updateUser\"")) {
                    return handleUpdateUser(request);
                } else {
                    return createErrorResponse("未知的操作类型");
                }
            } catch (Exception e) {
                System.err.println("处理请求时发生错误：" + e.getMessage());
                return createErrorResponse("服务器内部错误");
            }
        }

        /**
         * 处理用户注册请求
         * @param request 注册请求
         * @return 注册响应
         */
        private String handleRegister(String request) {
            try {
                // 简单解析JSON（实际项目中应使用JSON库）
                String username = extractValue(request, "username");
                String password = extractValue(request, "password");
                String email = extractValue(request, "email");

                if (username == null || password == null) {
                    return createErrorResponse("用户名和密码不能为空");
                }

                User user = new User(username, password, email);
                boolean success = dbManager.registerUser(user);

                if (success) {
                    return createSuccessResponse("注册成功");
                } else {
                    return createErrorResponse("注册失败，用户名可能已存在");
                }
            } catch (Exception e) {
                return createErrorResponse("注册请求格式错误");
            }
        }

        /**
         * 处理用户登录请求
         * @param request 登录请求
         * @return 登录响应
         */
        private String handleLogin(String request) {
            try {
                String username = extractValue(request, "username");
                String password = extractValue(request, "password");

                if (username == null || password == null) {
                    return createErrorResponse("用户名和密码不能为空");
                }

                User user = dbManager.loginUser(username, password);
                if (user != null) {
                    return createLoginSuccessResponse(user);
                } else {
                    return createErrorResponse("登陆失败，用户名或密码错误");
                }
            } catch (Exception e) {
                return createErrorResponse("登录请求格式错误");
            }
        }

        /**
         * 处理获取用户列表请求
         * @return 用户列表响应
         */
        private String handleGetUserList() {
            try {
                List<User> users = dbManager.getAllUsers();
                return createUserListResponse(users);
            } catch (Exception e) {
                return createErrorResponse("获取用户列表请求格式错误");
            }
        }

        /**
         * 处理删除用户请求
         * @param request 删除用户请求
         * @return 删除响应
         */
        private String handleDeleteUser(String request) {
            try {
                String username = extractValue(request, "username");
                String userIdStr = extractValue(request, "userIdStr");
                
                boolean success = false;
                if (username != null && !username.trim().isEmpty()) {
                    // 根据用户名删除
                    success = dbManager.deleteUserByUsername(username);
                } else if (userIdStr != null && !userIdStr.trim().isEmpty()) {
                    // 根据用户ID删除
                    try {
                        int userId = Integer.parseInt(userIdStr);
                        success = dbManager.deleteUserById(userId);
                    } catch (NumberFormatException e) {
                        return createErrorResponse("用户ID格式错误");
                    }
                } else {
                    return createErrorResponse("请提供用户名或用户ID");
                }

                if ( success) {
                    return createSuccessResponse("用户删除成功");
                } else {
                    return createErrorResponse("用户删除失败");
                }
            } catch (Exception e) {
                return createErrorResponse("删除用户请求格式错误");
            }
        }

        /**
         * 处理根据用户名获取用户请求
         * @param request 获取用户请求
         * @return 获取用户响应
         */
        private String handleGetUserByUsername(String request) {
            try {
                String username = extractValue(request, "username");

                if (username == null || username.trim().isEmpty()) {
                    return createErrorResponse("用户名不能为空");
                }
                User user = dbManager.findUserByUsername(username);

                if (user != null) {
                    return createUserResponse(user);
                } else {
                    return createErrorResponse("用户不存在");
                }
            } catch (Exception e) {
                return createErrorResponse("获取用户请求格式错误");
            }
        }

        /**
         * 处理修改用户信息请求
         * @param request 修改用户请求
         * @return 修改用户响应
         */
        private String handleUpdateUser(String request) {
            try {
                String username = extractValue(request, "username");
                String newPassword = extractValue(request, "password");
                String newEmail = extractValue(request, "email");

                // 检查用户是否存在
                if(username == null || username.trim().isEmpty()){
                    return createErrorResponse("用户不存在");
                }

                // 检查是否有要修改的内容
                if((newPassword == null || newPassword.trim().isEmpty())&&
                        (newEmail == null || newEmail.trim().isEmpty())) {
                    return createErrorResponse("请提供要修改的信息（密码或邮箱）");
                }

                boolean updated = dbManager.updateUserInfo(username, newPassword, newEmail);
                if (updated) {
                    return createSuccessResponse("用户信息修改成功");
                } else {
                    return createErrorResponse("用户信息修改失败");
                }
            } catch (Exception e) {
                return createErrorResponse("修改用户请求格式错误");
            }
        }

        /**
         * 从JSON字符串中提取指定字段的值
         * @param json JSON字符串
         * @param key 字段名
         * @return 字段值
         */
        private String extractValue(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int startIndex = json.indexOf(pattern);
            if (startIndex == -1) return null;

            startIndex += pattern.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return null;

            return json.substring(startIndex, endIndex);
        }

        /**
         * 创建成功响应
         * @param message 成功消息
         * @return JSON响应字符串
         */
        private String createSuccessResponse(String message) {
            return "{\"status\":\"success\",\"message\":\"" + message + "\"}";
        }

        /**
         * 创建错误响应
         * @param message 错误消息
         * @return JSON响应字符串
         */
        private String createErrorResponse(String message) {
            return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
        }

        /**
         * 创建单个用户响应
         * @param user 用户对象
         * @return JSON响应字符串
         */
        private String createUserResponse(User user) {
            return "{\"status\":\"success\",\"data\":{" +
                    "\"id\":" + user.getId() + "," +
                    "\"username\":\"" + user.getUsername() + "\"," +
                    "\"email\":\"" + (user.getEmail() != null ? user.getEmail() : "") + "\"," +
                    "\"password\":\"" + user.getPassword() + "\"," +
                    "\"createdAt\":\"" + user.getCreatedAt() + "\"" +
                    "}}";
        }

        /**
         * 创建登录成功响应
         * @param user 用户对象
         * @return JSON响应字符串
         */
        private String createLoginSuccessResponse(User user) {
            return "{\"status\":\"success\",\"message\":\"登录成功\",\"data\":{" +
                    "\"id\":" + user.getId() + "," +
                    "\"username\":\"" + user.getUsername() + "\"," +
                    "\"email\":\"" + (user.getEmail() != null ? user.getEmail() : "") + "\"" +
                    "}}";
        }

        /**
         * 创建用户列表响应
         * @param users 用户列表
         * @return JSON响应字符串
         */
        private String createUserListResponse(List<User> users) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"status\":\"success\",\"data\":[");

            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                sb.append("{")
                        .append("\"id\":").append(user.getId()).append(",")
                        .append("\"username\":\"").append(user.getUsername()).append("\",")
                        .append("\"email\":\"").append(user.getEmail() != null ? user.getEmail() : "").append("\"")
                        .append("\"password\":\"").append(user.getPassword()).append("\",")
                        .append("\"createdAt\":\"").append(user.getCreatedAt()).append("\"")
                        .append("}");

                if (i < users.size() - 1) {
                    sb.append(",");
                }
            }

            sb.append("]}");
            return sb.toString();
        }

        /**
         * 关闭客户端连接
         */
        private void closeConnection() {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
                System.out.println("客户端连接已关闭: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.err.println("关闭客户端连接失败: " + e.getMessage());
            }
        }
    }

    /**
     * 主方法，启动服务器
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SocketServer server = new SocketServer();

        // 添加关闭钩子，确保服务器正常关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭服务器...");
            server.stop();
        }));

        // 启动服务器
        server.start();
    }
}


