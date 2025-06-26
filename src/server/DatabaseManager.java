package server;

import java.io.File;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:database/app.db";
    private Connection connection;

    //构造函数，初始化数据库连接
    public DatabaseManager() {
        try {
            //确保数据库目录存在
            File dbDir = new File("database");
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            //加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);

            //初始化数据库
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC驱动未找到: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
    }

    //初始化数据库表结构
    public void initializeDatabase() {
        try {
            Statement statement = connection.createStatement();
            String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL,
                    email TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("SQL初始化表错误: " + e.getMessage());
        }
    }

    /**
     * 用户注册
     * @param user 用户对象
     * @return 注册是否成功
     */
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, encryptPassword(user.getPassword() )); //使用SHA256对密码进行加密
            statement.setString(3, user.getEmail());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("用户注册成功: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            if(e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("用户名已存在: " + user.getUsername());
            } else {
                System.err.println("SQL用户注册错误: " + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 用户登陆
     * @param username 用户名
     * @param password 密码
     * @return 登录成功返回用户对象，失败返回null
     */
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? ";

        try(PreparedStatement statement = connection.prepareStatement(sql)) { // 预编译 然后执行
            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");

                //验证密码SHA2
                if(verifyPassword(password, storedPassword)) {
                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"),
                            rs.getTimestamp("created_at")
                    );
                    System.out.println("用户登陆成功: " + username);
                    return user;
                } else {
                    System.out.println("用户登录失败: 密码错误: " + username);
                }
            } else {
                System.out.println("用户登录失败: 用户不存在: " + username);
            }
        } catch (SQLException e) {
            System.err.println("登陆查询错失败: " + e.getMessage());
        }
        return null;
    }


    /**
     * 修改用户信息
     * @param username 用户名
     * @param newPassword 新密码（可为null表示不修改）
     * @param newEmail 新邮箱（可为null表示不修改）
     * @return 修改是否成功
     */
    public boolean updateUserInfo(String username, String newPassword, String newEmail) {
        StringBuilder sqlBuilder = new StringBuilder("UPDATE users SET ");
        List<String> updates = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if(newPassword != null && !newPassword.isEmpty()) {
            String encryptedPassword = encryptPassword(newPassword);
            updates.add("password = ?");
            parameters.add(encryptedPassword);
        }

        if(newEmail != null && !newEmail.isEmpty()) {
            updates.add("email = ?");
            parameters.add(newEmail);
        }

        if(updates.isEmpty()) {
            return false; // 没有更新
        }

        sqlBuilder.append(String.join( ", ", updates));
        sqlBuilder.append(" WHERE username = ?");
        parameters.add(username);

        try (PreparedStatement statement = connection.prepareStatement(sqlBuilder.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("用户信息修改成功: " + username);
                return true;
            } else {
                System.out.println("未找到要修改的用户: " + username);
            }
        } catch (SQLException e) {
            System.err.println("修改用户信息失败: " + e.getMessage());
        }
        return false;
    }



    /**
     * 获取所有用户列表
     * @return 用户列表
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try(Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
               User user = new User (
                       rs.getInt("id"),
                       rs.getString("username"),
                       rs.getString("password"),
                       rs.getString("email"),
                       rs.getTimestamp("created_at")
               );
               users.add(user);
            }
            System.out.println("获取所有用户成功，共 " + users.size() + " 个用户");
        } catch (SQLException e){
            System.err.println("获取所有用户错误: " + e.getMessage());
        }
        return users;
    }

    /**
     * 根据用户名查找用户 暂时用不到
     * @param username 用户名
     * @return 用户对象，未找到返回null
     */
    public User findUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getTimestamp("created_at")
                );
                System.out.println("用户查找成功: " + username);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("查找用户错误: " + e.getMessage());
        }
        return null;
    }

    /**
     * 根据用户ID删除用户
     * @param userId 用户ID
     * @return 删除是否成功
     */
    public boolean deleteUserById(int userId) {
        String sql = "DELETE FROM users WHERE id = ?";

        try(PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("用户删除成功: " + userId);
                return true;
            } else {
                System.out.println("需要删除的用户未找到: " + userId);
            }
        } catch (SQLException e) {
            System.err.println("用户删除错误: " + e.getMessage());
        }
        return false;
    }

    /**
     * 根据用户名删除用户
     * @param username 用户名
     * @return 删除是否成功
     */
    public boolean deleteUserByUsername(String username) {
        String sql = "DELETE FROM users WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);

            int rowsDeleted = statement.executeUpdate();

            if (rowsDeleted > 0) {
                System.out.println("用户已删除: " + username);
                return true;
            } else {
                System.out.println("未找到该用户: " + username);
            }
        } catch (SQLException e) {
            System.err.println("删除用户时出错: " + e.getMessage());
        }
        return false;
    }


    /**
     * 生成随机盐值
     * @return Base64编码的盐值
     */
    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用SHA-256和盐值加密密码
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    private String hashPasswordWithSalt(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // 将盐值和密码组合
            String saltedPassword = salt + password;

            // 计算哈希值
            byte[] hashedBytes = md.digest(saltedPassword.getBytes("UTF-8"));

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            System.err.println("密码加密失败: " + e.getMessage());
            // 如果加密失败，使用简单的备用方法
            return "fallback_" + password.hashCode();
        }
    }

    /**
     * 加密密码（包含盐值）
     * @param password 原始密码
     * @return 格式为"盐值:哈希值"的加密密码
     */
    private String encryptPassword(String password) {
        String salt = generateSalt();
        String hashedPassword = hashPasswordWithSalt(password, salt);
        return salt + ":" + hashedPassword;
    }

    /**
     * 验证密码
     * @param inputPassword 输入的密码
     * @param storedPassword 存储的加密密码（格式：盐值:哈希值）
     * @return 密码是否匹配
     */
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        try {
            // 分离盐值和哈希值
            String[] parts = storedPassword.split(":", 2);
            if (parts.length != 2) {
                // 密码格式不正确，拒绝登录
                System.err.println("密码格式不正确，请重新注册账户");
                return false;
            }

            String salt = parts[0];
            String storedHash = parts[1];

            // 使用相同的盐值加密输入密码
            String inputHash = hashPasswordWithSalt(inputPassword, salt);

            // 比较哈希值
            return storedHash.equals(inputHash);
        } catch (Exception e) {
            System.err.println("密码验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 关闭数据库连接
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("数据库连接已关闭");
            }
        } catch (SQLException e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
        /*
        try {
            if (connection != null && !connection.isClosed()) {
                //hook注入，jvm退出时关闭数据库连接
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        connection.close();
                        System.out.println("数据库连接已关闭");
                    } catch (SQLException e) {
                        System.err.println("数据库连接关闭失败");
                    }
                }));
            }
        } catch (SQLException e) {
            System.err.println("数据库连接关闭失败");
        }
        */
    }

    /**
     * 检查数据库连接是否有效
     * @return 连接是否有效
     */
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
