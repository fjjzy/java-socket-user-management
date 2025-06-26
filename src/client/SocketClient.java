package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Socket客户端类
 * 负责与服务器建立连接并进行通信
 */
public class SocketClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private boolean isConnected = false;

    /**
     * 连接到服务器
     * @return 连接是否成功
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            isConnected = true;

            System.out.println("成功连接到服务器: " + SERVER_HOST + ":" + SERVER_PORT);
            return true;
        } catch (IOException e) {
            System.err.println("连接服务器失败: " + e.getMessage());
            isConnected = false;
            return false;
        }
    }

    /**
     * 断开与服务器的连接
     */
    public void disconnect() {
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null && socket.isConnected()) {
                socket.close();
            }
            isConnected = false;
            System.out.println("已断开与服务器的连接");
        } catch (IOException e) {
            System.err.println("断开连接失败: " + e.getMessage());
        }
    }

    /**
     * 发送请求到服务器并获取响应
     * @param request 请求字符串
     * @return 服务器响应字符串
     */
    public String sendRequest(String request) {
        if(!isConnected) {
            return createErrorResponse("未连接到服务器");
        }
        try {
            writer.println(request);
            //使用 writer.write(request) 可能不会发送换行符，而服务器端使用 reader.readLine() 读取数据时需要换行符作为结束标志。应该使用 writer.println(request) 或 writer.write(request + "\n") 。
            System.out.println("发送请求: " + request);

            String response = reader.readLine();
            System.out.println("接收响应: " + response);

            return response;
        } catch (IOException e) {
            System.err.println("发送请求失败: " + e.getMessage());
            return createErrorResponse("发送请求失败: " + e.getMessage());
        }
    }

    /**
     * 用户注册请求
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return 服务器响应
     */
    public String register(String username, String password, String email) {
        String request = createRegisterRequest(username, password, email);
        return sendRequest(request);
    }

    /**
     * 用户登录请求
     * @param username 用户名
     * @param password 密码
     * @return 服务器响应
     */
    public String login(String username, String password) {
        String request = createLoginRequest(username, password);
        return sendRequest(request);
    }

    /**
     * 获取用户列表请求
     * @return 服务器响应
     */
    public String getUserList() {
        String request = createGetUserListRequest();
        return sendRequest(request);
    }

    /**
     * 删除用户请求
     * @param username 要删除的用户名
     * @return 服务器响应
     */
    public String deleteUser(String username) {
        String request = createDeleteUserRequest(username);
        return sendRequest(request);
    }

    /**
     * 根据用户名获取用户信息请求
     * @param username 用户名
     * @return 服务器响应
     */
    public String getUserByUsername(String username) {
        String request = createGetUserByUsernameRequest(username);
        return sendRequest(request);
    }

    /**
     * 修改用户信息请求
     * @param username 用户名
     * @param newPassword 新密码（可为null表示不修改）
     * @param newEmail 新邮箱（可为null表示不修改）
     * @return 服务器响应
     */
    public String updateUser(String username, String newPassword, String newEmail) {
        String request = createUpdateUserRequest(username, newPassword, newEmail);
        return sendRequest(request);
    }

    /**
     * 创建注册请求JSON字符串
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return JSON请求字符串
     */
    private String createRegisterRequest(String username, String password, String email) {
        return "{\"action\":\"register\",\"data\":{" +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"," +
                "\"email\":\"" + (email != null ? email : "") + "\"" +
                "}}";
    }

    /**
     * 创建登录请求JSON字符串
     * @param username 用户名
     * @param password 密码
     * @return JSON请求字符串
     */
    private String createLoginRequest(String username, String password) {
        return "{\"action\":\"login\",\"data\":{" +
                "\"username\":\"" + username + "\"," +
                "\"password\":\"" + password + "\"" +
                "}}";
    }

    /**
     * 创建获取用户列表请求JSON字符串
     * @return JSON请求字符串
     */
    private String createGetUserListRequest() {
        return "{\"action\":\"getUserList\"}";
    }

    /**
     * 创建删除用户请求JSON字符串
     * @param username 要删除的用户名
     * @return JSON请求字符串
     */
    private String createDeleteUserRequest(String username) {
        return "{\"action\":\"deleteUser\",\"data\":{" +
                "\"username\":\"" + username + "\"" +
                "}}";
    }

    /**
     * 创建根据用户名获取用户请求JSON字符串
     * @param username 用户名
     * @return JSON请求字符串
     */
    private String createGetUserByUsernameRequest(String username) {
        return "{\"action\":\"getUserByUsername\",\"data\":{" +
                "\"username\":\"" + username + "\"" +
                "}}";
    }

    /**
     * 创建修改用户信息请求JSON字符串
     * @param username 用户名
     * @param newPassword 新密码（可为null表示不修改）
     * @param newEmail 新邮箱（可为null表示不修改）
     * @return JSON请求字符串
     */
    private String createUpdateUserRequest(String username, String newPassword, String newEmail) {
        StringBuilder request = new StringBuilder();
        request.append("{\"action\":\"updateUser\",\"data\":{")
                .append("\"username\":\"").append(username).append("\"");

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            request.append(",\"password\":\"").append(newPassword).append("\"");
        }

        if (newEmail != null && !newEmail.trim().isEmpty()) {
            request.append(",\"email\":\"").append(newEmail).append("\"");
        }

        request.append("}}");
        return request.toString();
    }


    /**
     * 创建错误响应JSON字符串
     * @param message 错误消息
     * @return JSON错误响应字符串
     */
    private String createErrorResponse(String message) {
        return "{\"status\":\"error\",\"message\":\"" + message + "\"}";
    }

    /**
     * 检查是否连接到服务器
     * @return 连接状态
     */
    public boolean isConnected() {
        return isConnected && socket != null && socket.isConnected();
    }

    /**
     * 解析响应状态
     * @param response 服务器响应
     * @return 是否成功
     */
    public boolean isResponseSuccess(String response) {
        return response != null && response.contains("\"status\":\"success\"");
    }

    /**
     * 从响应中提取消息
     * @param response 服务器响应
     * @return 消息内容
     */
    public String extractMessage(String response) {
        if (response == null) return "无响应";

        String pattern = "\"message\":\"";
        int startIndex = response.indexOf(pattern);
        if (startIndex == -1) return "解析响应失败";

        startIndex += pattern.length();
        int endIndex = response.indexOf("\"", startIndex);
        if (endIndex == -1) return "解析响应失败";

        return response.substring(startIndex, endIndex);
    }

    /**
     * 从登录响应中提取用户信息
     * @param response 登录响应
     * @return 用户信息字符串
     */
    public String extractUserInfo(String response) {
        if (response == null || !isResponseSuccess(response)) {
            return null;
        }

        try {
            // 简单解析用户信息
            String username = extractValueFromResponse(response, "username");
            String email = extractValueFromResponse(response, "email");

            StringBuilder userInfo = new StringBuilder();
            userInfo.append("用户名: ").append(username != null ? username : "未知");
            if (email != null && !email.isEmpty()) {
                userInfo.append("\n邮箱: ").append(email);
            }

            return userInfo.toString();
        } catch (Exception e) {
            return "解析用户信息失败";
        }
    }

    /**
     * 从响应中提取指定字段的值
     * @param response 响应字符串
     * @param key 字段名
     * @return 字段值
     */
    private String extractValueFromResponse(String response, String key) {
        String pattern = "\"" + key + "\":\"";
        int startIndex = response.indexOf(pattern);
        if (startIndex == -1) return null;

        startIndex += pattern.length();
        int endIndex = response.indexOf("\"", startIndex);
        if (endIndex == -1) return null;

        return response.substring(startIndex, endIndex);
    }

    /**
     * 解析用户列表响应
     * @param response 服务器响应
     * @return 用户列表字符串数组
     */
    public String[] parseUserList(String response) {
        if (response == null || !isResponseSuccess(response)) {
            return new String[0];
        }

        try {
            java.util.List<String> users = new java.util.ArrayList<>();

            // 查找数据数组开始位置
            int dataStart = response.indexOf("\"data\":[")
                    ;
            if (dataStart == -1) return new String[0];

            int arrayStart = response.indexOf("[", dataStart);
            if (arrayStart == -1) return new String[0];

            // 手动解析JSON数组
            int pos = arrayStart + 1;
            int braceCount = 0;
            StringBuilder currentObject = new StringBuilder();
            boolean inString = false;
            boolean escapeNext = false;

            while (pos < response.length()) {
                char c = response.charAt(pos);

                if (escapeNext) {
                    currentObject.append(c);
                    escapeNext = false;
                } else if (c == '\\') {
                    currentObject.append(c);
                    escapeNext = true;
                } else if (c == '"') {
                    currentObject.append(c);
                    inString = !inString;
                } else if (!inString) {
                    if (c == '{') {
                        braceCount++;
                        currentObject.append(c);
                    } else if (c == '}') {
                        braceCount--;
                        currentObject.append(c);

                        if (braceCount == 0) {
                            // 完整的用户对象
                            String userObj = currentObject.toString();
                            String username = extractJsonValue(userObj, "username");
                            String email = extractJsonValue(userObj, "email");

                            if (username != null) {
                                String userInfo = username;
                                if (email != null && !email.isEmpty()) {
                                    userInfo += " (" + email + ")";
                                }
                                users.add(userInfo);
                            }

                            currentObject.setLength(0);
                        }
                    } else if (c == ']') {
                        // 数组结束
                        break;
                    } else if (c != ',' && c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                        currentObject.append(c);
                    }
                } else {
                    currentObject.append(c);
                }

                pos++;
            }

            return users.toArray(new String[0]);
        } catch (Exception e) {
            System.err.println("解析用户列表失败: " + e.getMessage());
            return new String[0];
        }
    }

    /**
     * 从JSON对象字符串中提取指定字段的值
     * @param jsonObj JSON对象字符串
     * @param key 字段名
     * @return 字段值
     */
    private String extractJsonValue(String jsonObj, String key) {
        String pattern = "\"" + key + "\":";
        int startIndex = jsonObj.indexOf(pattern);
        if (startIndex == -1) return null;

        startIndex += pattern.length();

        // 跳过空格
        while (startIndex < jsonObj.length() && Character.isWhitespace(jsonObj.charAt(startIndex))) {
            startIndex++;
        }

        if (startIndex >= jsonObj.length()) return null;

        // 检查值的类型
        if (jsonObj.charAt(startIndex) == '"') {
            // 字符串值
            startIndex++; // 跳过开始的引号
            int endIndex = startIndex;
            boolean escapeNext = false;

            while (endIndex < jsonObj.length()) {
                char c = jsonObj.charAt(endIndex);
                if (escapeNext) {
                    escapeNext = false;
                } else if (c == '\\') {
                    escapeNext = true;
                } else if (c == '"') {
                    break;
                }
                endIndex++;
            }

            if (endIndex < jsonObj.length()) {
                return jsonObj.substring(startIndex, endIndex);
            }
        } else {
            // 数字或其他值
            int endIndex = startIndex;
            while (endIndex < jsonObj.length() &&
                    jsonObj.charAt(endIndex) != ',' &&
                    jsonObj.charAt(endIndex) != '}' &&
                    !Character.isWhitespace(jsonObj.charAt(endIndex))) {
                endIndex++;
            }

            if (endIndex > startIndex) {
                return jsonObj.substring(startIndex, endIndex);
            }
        }

        return null;
    }

}
