package server;

import java.sql.Timestamp;

public class User {
    private int id;
    private  String username;
    private String password;
    private String email;
    private Timestamp createdAt;

    public User() {
    }

    /**
     * 完整的构造函数
     * @param id
     * @param username
     * @param password
     * @param email
     * @param createdAt
     */
    public User(int id, String username, String password, String email, Timestamp createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.createdAt = createdAt;
    }

    /**
     * 创建用户时使用的构造函数
     * @param username
     * @param password
     * @param email
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        int var1 = this.id;
        var1 = 31 * var1 + (this.username != null ? this.username.hashCode() : 0);
        return var1;
    }

    /**
     * 重写toString方法, 用户信息字符串
     * @return 用户信息字符串
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\''+
                ", email='" + email + '\''+
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * 重写equals方法
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;

        User user = new User();
        return id == user.id &&
                username != null ? username.equals(user.username) : user.username == null;
    }

}