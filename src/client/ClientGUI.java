package client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    //组件声明
    private SocketClient socketClient; // Socket客户端实例，用于与服务器通信
    private JTabbedPane tabbedPane; // 主选项卡面板，用于切换不同功能页面

    //登录面板组件
    private JTextField loginUsernameField; // 登录用户名输入框
    private JPasswordField loginPasswordField; // 登录密码输入框
    private JButton loginButton; // 登录按钮
    private JButton connectButton; // 连接按钮
    private JLabel connectionStatusLable; // 连接状态标签

    //注册面板组件
    private JTextField registerUsernameField; // 注册用户名输入框
    private JPasswordField registerPasswordField; // 注册密码输入框
    private JTextField registerEmailField; // 注册邮箱输入框
    private JButton registerButton; // 注册按钮

    //用户列表面板组件
    private JList<String> userList; // 用户列表
    private DefaultListModel<String> userListModel; // 用户列表模型
    private DefaultListModel<String> allUsersModel; // 存储所有用户数据，用于搜索功能
    private JTextField searchField; // 搜索框
    private JButton searchButton; // 搜索按钮
    private JButton clearSearchButton; // 清空搜索按钮
    private JButton refreshButton; // 刷新按钮
    private JButton viewUserInfoButton; // 查看用户信息按钮
    private JButton editUserButton; // 编辑用户按钮
    private JButton deleteUserButton; // 删除用户按钮

    //状态栏
    private JLabel statusLabel; // 状态栏标签

    //构造函数
    public ClientGUI() {
        //初始化Socket客户端
        socketClient = new SocketClient();
        initializeGUI();
        setupEventHandlers();
        
        // 自动连接服务器
        SwingUtilities.invokeLater(() -> {
            connectToServer();
        });
    }

    private void initializeGUI() {
        // 初始化组件
        // 设置窗口属性
        setTitle("Java Socket 客户端");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置关闭窗口时退出程序
        setLocationRelativeTo(null); // 设置窗口在屏幕中央显示
        //setResizable(false); // 设置窗口大小不可调整

        // 创建主面板
        Container mainContainer = getContentPane();
        mainContainer.setLayout(new BorderLayout());

        // 创建选项卡面板
        tabbedPane = new JTabbedPane();

        // 添加各个面板
        tabbedPane.add("连接", createConnectionPanel());
        //添加用户登录面板
        tabbedPane.add("登录", createLoginPanel());
        //添加用户注册面板
        tabbedPane.add("注册", createRegisterPanel());
        //添加用户列表面板
        tabbedPane.add("用户列表", createUserListPanel());

        //添加状态栏
        statusLabel = new JLabel("就绪");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder()); // 为状态标签设置凹陷边框

        //添加组件到主面板
        mainContainer.add(statusLabel, BorderLayout.SOUTH); // 将选项卡面板添加到主面板中央
        mainContainer.add(tabbedPane, BorderLayout.CENTER); // 将状态标签添加到主面板底部

        //初始化状态设置
        updateConnectionStatus(); // 更新连接状态显示
    }

    /**
     * 创建连接面板
     *
     * @return 连接面板
     */
    private JPanel createConnectionPanel() { // 创建连接服务器面板的方法
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 连接状态标签
        gbc.gridx = 0;
        gbc.gridy = 0; // 设置标签的位置0行0列
        gbc.gridwidth = 2; // 标签占两列
        gbc.insets = new Insets(10, 10, 10, 10); // 设置标签的间距
        connectionStatusLable = new JLabel("未连接", JLabel.CENTER); // 创建标签 显示连接状态 居中
        connectionStatusLable.setForeground(Color.RED); // 设置标签颜色
        connectionStatusLable.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(connectionStatusLable, gbc);

        // 连接按钮
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 1; // 列宽
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        connectButton = new JButton("连接服务器");
        connectButton.setPreferredSize(new Dimension(120, 30)); // 设置按钮首选大小
        panel.add(connectButton, gbc);

        // 断开按钮
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JButton disconnectButton = new JButton("断开连接");
        disconnectButton.setPreferredSize(new Dimension(120, 30));
        disconnectButton.addActionListener(e -> { // 添加点击监听器
            socketClient.disconnect();
            updateConnectionStatus(); //更新连接状态显示
            updateStatus("已断开连接"); // 更新状态栏信息
        });
        panel.add(disconnectButton, gbc);

        // 服务器信息
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 0, 0);
        JLabel serverInfoLabel = new JLabel("<html><center>服务器地址: localhost:8888<br>请确保服务器已启动</center></html>", JLabel.CENTER);
        serverInfoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(serverInfoLabel, gbc);

        return panel;
    }

    /**
     * 创建登录面板
     *
     * @return 登录面板
     */
    private JPanel createLoginPanel() { // 创建用户登录面板的方法
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 用户名标签和输入框
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST; // 将标签右对齐
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel usernameLabel = new JLabel("用户名:");
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginUsernameField = new JTextField(15);
        panel.add(loginUsernameField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel passwordLabel = new JLabel("密码:");
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        loginPasswordField = new JPasswordField(15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(loginPasswordField, gbc);

        // 登录按钮
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; // 设置不填充
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 10, 10);
        loginButton = new JButton("登录");
        loginButton.setPreferredSize(new Dimension(100, 30));  // 设置按钮首选大小

        panel.add(loginButton, gbc);

        return panel;
    }

    /**
     * 创建注册面板
     *
     * @return 注册面板
     */
    private JPanel createRegisterPanel() { // 创建用户注册面板的方法
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 用户名标签和输入框
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel usernameLabel = new JLabel("用户名:");
        panel.add(usernameLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        registerUsernameField = new JTextField(15);
        panel.add(registerUsernameField, gbc);

        // 密码标签和输入框
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel passwordLabel = new JLabel("密码:");
        panel.add(passwordLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        registerPasswordField = new JPasswordField(15);
        panel.add(registerPasswordField, gbc);

        // 邮箱标签和输入框
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(5, 5, 5, 5);
        JLabel emailLabel = new JLabel("邮箱:");
        panel.add(emailLabel, gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        registerEmailField = new JTextField(15);
        panel.add(registerEmailField, gbc);

        // 注册按钮
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; // 设置不填充
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 10, 10, 10);
        registerButton = new JButton("注册");
        registerButton.setPreferredSize(new Dimension(100, 30));  // 设置按钮首选大小
        panel.add(registerButton, gbc);

        return panel;
    }

    /**
     * 创建用户列表面板
     *
     * @return 用户列表面板
     */
    private JPanel createUserListPanel() { // 创建用户列表管理面板的方法
        JPanel panel = new JPanel(new BorderLayout()); // 创建面板，使用边界布局

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createTitledBorder("搜索用户"));

        searchField = new JTextField(20);// 创建搜索输入框，宽度为15个字符
        searchButton = new JButton("搜索");
        clearSearchButton = new JButton("清空");


        searchPanel.add(new JLabel("用户名:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearSearchButton);

        // 创建用户列表
        userListModel = new DefaultListModel<>();
        allUsersModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.setBorder(BorderFactory.createTitledBorder("用户列表"));

        // 添加滚动面板
        JScrollPane scrollPane = new JScrollPane(userList);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));// 创建按钮面板，使用流式布局
        refreshButton = new JButton("刷新列表");
        viewUserInfoButton = new JButton("查看信息");
        editUserButton = new JButton("编辑用户");
        deleteUserButton = new JButton("删除用户");

        buttonPanel.add(refreshButton);
        buttonPanel.add(viewUserInfoButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);

        // 添加组件
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 设置事件处理器
     * 为各个按钮和输入框添加事件监听器
     */
    private void setupEventHandlers() { // 设置各种事件处理器的方法

        // 连接按钮事件
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        // 登录按钮事件
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });

        // 注册按钮事件
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performRegister();
            }
        });

        // 刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                refreshUserList();
            }
        });

        // 查看用户信息按钮事件
        viewUserInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewSelectedUserInfo();
            }
        });

        // 删除用户按钮事件
        deleteUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedUser();
            }
        });

        // 修改用户信息按钮事件
        editUserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedUser();
            }
        });

        // 搜索按钮事件
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        // 清除搜索按钮事件
        clearSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearSearch();
            }
        });

        // 搜索框回车事件
        searchField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        // 回车键登录
        loginPasswordField.addActionListener(new ActionListener() { // 为登录密码输入框添加回车事件监听器
            @Override
            public void actionPerformed(ActionEvent e) { // 重写动作执行方法
                performLogin(); // 调用执行登录方法
            }
        });

        // 选项卡切换事件 - 首次打开用户列表时自动刷新
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            // 检查是否切换到用户列表面板（索引为3）
            if (selectedIndex == 3 && socketClient.isConnected()) {
                // 检查用户列表是否为空，如果为空则自动刷新
                if (userListModel.isEmpty()) {
                    refreshUserList();
                }
            }
        });

        // 窗口关闭事件
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                socketClient.disconnect();
            }
        });
    }

    /**
     * 连接到服务器
     * 尝试建立与服务器的Socket连接
     */
    private void connectToServer() { // 连接到服务器的方法
        updateStatus("正在连接服务器...   ");

        if(socketClient.connect()) {
            updateConnectionStatus();
            updateStatus("已连接服务器！");
            //JOptionPane.showMessageDialog(this, "已连接服务器！", "提示", JOptionPane.INFORMATION_MESSAGE);
        } else {
            updateConnectionStatus();
            updateStatus("连接服务器失败！");
            JOptionPane.showMessageDialog(this, "连接服务器失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 执行登录操作
     * 验证输入并发送登录请求到服务器
     */
    private void performLogin() { // 执行用户登录操作的方法
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整的登录信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateStatus("正在登录...");
        String response = socketClient.login(username, password);

        if (socketClient.isResponseSuccess(response)) {
            String userInfo = socketClient.extractUserInfo(response);
            updateStatus("登录成功！");
            JOptionPane.showMessageDialog(this, userInfo, "登录成功", JOptionPane.INFORMATION_MESSAGE);

            //清空输入框
            loginUsernameField.setText("");
            loginPasswordField.setText("");

            //切换到用户列表管理面板 并刷新
            tabbedPane.setSelectedIndex(3);
            refreshUserList();
        }
        else {
            String message = socketClient.extractMessage(response);
            updateStatus("登录失败");
            JOptionPane.showMessageDialog(this, message, "登录失败", JOptionPane.ERROR_MESSAGE);
        }
    }



    /**
     * 执行注册操作
     * 验证输入并发送注册请求到服务器
     */
    private void performRegister() { // 执行用户注册操作的方法
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String email = registerEmailField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整的注册信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateStatus("正在注册...");

        String response = socketClient.register(username, password, email); // 发送注册请求

        if (socketClient.isResponseSuccess(response)) {
            updateStatus("注册成功！");
            JOptionPane.showMessageDialog(this, "注册成功！", "提示", JOptionPane.INFORMATION_MESSAGE);

            // 清空输入框
            registerUsernameField.setText(""); // 清空用户名输入框
            registerPasswordField.setText(""); // 清空密码输入框
            registerEmailField.setText(""); // 清空邮箱输入框

            // 切换到登录页面
            tabbedPane.setSelectedIndex(1); // 切换到登录页面
        } else {
            String message = socketClient.extractMessage(response);
            updateStatus("注册失败！");
            JOptionPane.showMessageDialog(this, "注册失败！\n" + message, "注册失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 刷新用户列表
     * 从服务器获取所有用户信息并更新列表显示
     */
    private void refreshUserList() { // 刷新用户列表的方法
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        updateStatus("正在获取用户列表...   ");

        String response = socketClient.getUserList() ;

        if (socketClient.isResponseSuccess(response)) {
            String[] users = socketClient.parseUserList(response);

            allUsersModel.clear(); // 清空所有用户
            userListModel.clear(); // 清空用户列表

            for (String user : users) {
                allUsersModel.addElement(user); // 添加所有用户
                userListModel.addElement(user); // 添加用户列表
            }

            searchField.setText(""); // 清空搜索框

            updateStatus("用户列表已更新 (" + users.length + " 个用户)"); // 更新状态栏显示用户列表更新成功及用户数量
        } else {// 如果获取失败
            String message = socketClient.extractMessage(response); // 从响应中提取错误消息
            updateStatus("获取用户列表失败"); // 更新状态栏显示获取失败
            JOptionPane.showMessageDialog(this, "获取用户列表失败！\n" + message, "获取失败", JOptionPane.ERROR_MESSAGE); // 显示获取失败对话框
        }
    }

    /**
     * 删除选中的用户
     * 确认后向服务器发送删除请求
     */
    private void deleteSelectedUser() { // 删除选中用户的方法
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器！", "错误", JOptionPane.ERROR_MESSAGE);
            return; //返回，不执行删除操作
        }

        String selectedUser = userList.getSelectedValue();
        if (selectedUser == null || selectedUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择一个用户！", "错误", JOptionPane.ERROR_MESSAGE);
            return; //返回，不执行删除操作
        }

        // 从用户信息中提取用户名（格式："用户名 (邮箱)"）
        String username = selectedUser; // 初始化用户名为选中的用户字符串
        int spaceIndex = selectedUser.indexOf(" ("); // 查找空格和左括号的位置
        if (spaceIndex > 0) { // 如果找到了分隔符
            username = selectedUser.substring(0, spaceIndex); // 提取用户名部分
        }

        // 确认删除
        int result = JOptionPane.showConfirmDialog(this,
                "确定要删除用户 " + username + " 吗？", "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if(result != JOptionPane.YES_OPTION) {
            return;
        }

        updateStatus("正在删除用户 " + username + "...");

        String response = socketClient.deleteUser(username);

        if (socketClient.isResponseSuccess(response)) {
            updateStatus("删除用户成功！");
            JOptionPane.showMessageDialog(this,
                    "删除用户 " +username+" 成功！",
                    "成功",
                    JOptionPane.INFORMATION_MESSAGE
            );

            refreshUserList();
        } else {
            String message = socketClient.extractMessage(response);
            updateStatus("删除用户失败" );
            JOptionPane.showMessageDialog(this,
                    message, "错误",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * 查看选中用户的详细信息
     * 从服务器获取用户详细信息并显示在对话框中
     */
    private void viewSelectedUserInfo() { // 查看选中用户详细信息的方法
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器！", "错误", JOptionPane.ERROR_MESSAGE);
            return; //返回，不执行查看操作
        }

        String selectedUser = userList.getSelectedValue();
        if (selectedUser == null || selectedUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择一个用户！", "错误", JOptionPane.ERROR_MESSAGE);
            return; //返回，不执行查看操作
        }

        //从用户信息中提取用户名（格式："用户名 (邮箱)"）
        String username = selectedUser; // 初始化用户名为选中的用户字符串
        int index = selectedUser.indexOf(" ("); // 查找空格和左括号的位置
        if (index != -1) {
            username = selectedUser.substring(0, index);
        }

        updateStatus("正在获取用户信息...");

        String response = socketClient.getUserByUsername(username);

        if (!socketClient.isResponseSuccess(response)) {
            updateStatus("获取用户信息失败！");
            showUserInfoDialog( response);
            JOptionPane.showMessageDialog(this, "获取用户信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
        } else {
            updateStatus("获取用户信息成功！");
            showUserInfoDialog( response);
        }
    }

    /**
     * 显示用户信息对话框
     * 在模态对话框中显示用户的详细信息
     *
     * @param response 服务器响应
     */
    private void showUserInfoDialog(String response) { // 显示用户详细信息对话框的方法
        try {// 尝试解析用户信息
            // 简单解析JSON响应
            String id = extractJsonValue(response, "id"); // 提取用户ID
            String username = extractJsonValue(response, "username"); // 提取用户名
            String email = extractJsonValue(response, "email"); // 提取邮箱
            String createdAt = extractJsonValue(response, "createdAt"); // 提取创建时间

            StringBuilder info = new StringBuilder();
            //info.append("用户详细信息:\n\n"); // 添加标题
            info.append("\n用户ID: ").append(id != null ? id : "未知").append("\n"); // 添加用户ID信息
            info.append("用户名: ").append(username != null ? username : "未知").append("\n"); // 添加用户名信息
            info.append("邮箱: ").append(email != null && !email.isEmpty() ? email : "未设置").append("\n"); // 添加邮箱信息
            info.append("创建时间: ").append(createdAt != null ? createdAt : "未知"); // 添加创建时间信息

            JOptionPane.showMessageDialog(this, info.toString(), "用户信息", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
           JOptionPane.showMessageDialog(this, "解析用户信息失败", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 从JSON字符串中提取指定字段的值
     * 简单的JSON解析方法，用于提取键值对
     *
     * @param json JSON字符串
     * @param key  字段名
     * @return 字段值
     */
    private String extractJsonValue(String json, String key) { // 从JSON字符串中提取指定键值的方法
        String searchKey = "\"" + key + "\":"; // 构建搜索的键格式（带引号和冒号）
        int startIndex = json.indexOf(searchKey); // 查找键在JSON字符串中的位置
        if (startIndex == -1) { // 如果没有找到键
            return null; // 返回null
        }

        startIndex += searchKey.length(); // 移动到键值部分的开始位置

        // 跳过空格
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) { // 跳过键值前的空白字符
            startIndex++; // 移动索引
        }

        if (startIndex >= json.length()) { // 如果已经到达字符串末尾
            return null; // 返回null
        }

        char firstChar = json.charAt(startIndex); // 获取值的第一个字符
        int endIndex; // 声明结束索引变量

        if (firstChar == '\"') { // 如果值是字符串类型（以引号开始）
            // 字符串值
            startIndex++; // 跳过开始的引号
            endIndex = json.indexOf('\"', startIndex); // 查找结束引号的位置
            if (endIndex == -1) { // 如果没有找到结束引号
                return null; // 返回null
            }
            return json.substring(startIndex, endIndex); // 返回引号之间的字符串
        } else { // 如果值是数字或其他类型
            // 数字或其他值
            endIndex = startIndex; // 初始化结束索引
            while (endIndex < json.length() && // 查找值的结束位置
                    json.charAt(endIndex) != ',' && // 不是逗号
                    json.charAt(endIndex) != '}' && // 不是右大括号
                    json.charAt(endIndex) != ']') { // 不是右方括号
                endIndex++; // 移动结束索引
            }
            return json.substring(startIndex, endIndex).trim(); // 返回值并去除首尾空格
        }
    }

    /**
     * 执行用户搜索
     * 根据搜索框内容过滤用户列表
     */
    private void performSearch() { // 执行用户搜索的方法
        String searchText = searchField.getText().trim().toLowerCase();

        userListModel.clear(); // 清空用户列表显示模型

        if(searchText.isEmpty()) {
            for (int i = 0; i < allUsersModel.getSize(); i++) {
                userListModel.addElement(allUsersModel.getElementAt(i));
            }
            updateStatus("已显示所有用户 (" + allUsersModel.getSize() + ") 个用户");
        } else {
            // 根据用户名进行搜索
            int matchCount = 0; // 初始化匹配用户计数器
            for (int i = 0; i < allUsersModel.getSize(); i++) {
                String user = allUsersModel.getElementAt(i);
                String username = user;

                // 提取用户名（格式："用户名 (邮箱)"）
                int index = user.indexOf(" ("); // 查找空格和左括号的位置
                if (index != -1) { // 如果找到了分隔符
                    username = user.substring(0, index); // 提取用户名部分
                }

                // 检查用户名是否包含搜索文本
                if (username.toLowerCase().contains(searchText)) {
                    userListModel.addElement(user); // 将匹配的用户添加到用户列表显示模型
                    matchCount++; // 增加匹配用户计数器
                }
            }

            updateStatus("已显示 " + matchCount + " 个匹配用户 (" + allUsersModel.getSize() + ") 个用户");
        }
    }

    /**
     * 清除搜索
     * 清空搜索框并重新显示所有用户
     */
    private void clearSearch() { // 清除搜索的方法
        searchField.setText("");

        userListModel.clear(); // 清空用户列表显示模型
        for (int i = 0; i < userListModel.getSize(); i++) { // 遍历所有用户数据模型
            userListModel.addElement(allUsersModel.getElementAt(i)); // 将所有用户数据模型添加到用户列表显示模型
        }
        refreshUserList();
        updateStatus("已清除搜索，显示所有用户");
    }

    /**
     * 编辑选中的用户
     * 获取用户信息并显示编辑对话框
     */
    private void editSelectedUser() { // 编辑选中用户的方法//
        if (!socketClient.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接服务器", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedUser = userList.getSelectedValue();
        if (selectedUser == null || selectedUser.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择一个用户", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 从用户信息中提取用户名（格式："用户名 (邮箱)"）
        String username = selectedUser; // 初始化用户名为选中的用户字符串
        int spaceIndex = selectedUser.indexOf(" ("); // 查找空格和左括号的位置
        if (spaceIndex > 0) { // 如果找到了分隔符
            username = selectedUser.substring(0, spaceIndex); // 提取用户名部分
        }

        //获取用户信息
        String userInfo = socketClient.getUserByUsername(username);
        if (!socketClient.isResponseSuccess(userInfo)) {
            JOptionPane.showMessageDialog(this, socketClient.extractMessage(userInfo), "错误", JOptionPane.ERROR_MESSAGE);
            updateStatus("获取用户信息失败");
        } else {
            showEditUserDialog(userInfo);
            updateStatus("准备编辑用户");
        }
    }

    /**
     * 显示修改用户信息对话框
     * 创建包含用户信息编辑表单的模态对话框
     * @param userInfo 用户信息响应字符串
     */
    private void showEditUserDialog(String userInfo) { // 显示编辑用户信息对话框的方法
        JDialog dialog = new JDialog(this, "编辑用户信息", true);
        dialog.setLayout(new BorderLayout()); // 使用边界布局
        dialog.setSize(400, 300); // 设置对话框大小
        dialog.setLocationRelativeTo(this); // 设置对话框位置相对于主窗口居中

        // 解析用户信息
        String username = extractJsonValue(userInfo, "username");
        String email = extractJsonValue(userInfo, "email");

        // 创建表单面板
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // 创建表单面板
         gbc.gridx = 0; gbc.gridy = 0;
         formPanel.add(new JLabel("用户名:"), gbc);
         gbc.gridx = 1;
         gbc.anchor = GridBagConstraints.WEST; // 设置组件在网格单元格内左对齐
         JLabel usernameLabel = new JLabel(username);
         usernameLabel.setFont(usernameLabel.getFont().deriveFont(Font.BOLD));
         formPanel.add(usernameLabel, gbc);

        // 新密码
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("新密码:"), gbc);
        gbc.gridx = 1;
        JPasswordField newPasswordField = new JPasswordField(15);
        formPanel.add(newPasswordField, gbc);

        // 确认密码
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("确认密码:"), gbc);
        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        formPanel.add(confirmPasswordField, gbc);

        // 新邮箱
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("新邮箱:"), gbc);
        gbc.gridx = 1;
        JTextField newEmailField = new JTextField(15);
        if (email != null && !email.isEmpty()) {
            newEmailField.setText(email);
        }
        formPanel.add(newEmailField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton updateButton = new JButton("更新");
        JButton cancelButton = new JButton("取消");

        updateButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            if (!newPassword.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "新密码和确认密码不一致", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (newPassword.isEmpty() && !email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请至少修改一项信息！", "输入错误", JOptionPane.WARNING_MESSAGE); // 显示至少修改一项的警告
                return; // 返回，不执行更新
            }

            updateStatus("正在更新用户信息...");

            String response = socketClient.updateUser(username, newPassword, newEmailField.getText());

            if (!socketClient.isResponseSuccess(response)) {
                updateStatus("更新用户信息失败");
                JOptionPane.showMessageDialog(this, "修改用户信息失败！\n" + socketClient.extractMessage(response), "修改失败", JOptionPane.ERROR_MESSAGE);
            } else {
                updateStatus("用户信息更新成功");
                JOptionPane.showMessageDialog(this, "用户信息更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose(); // 关闭对话框
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose()); // 为取消按钮添加关闭对话框的事件监听器

        buttonPanel.add(updateButton); // 将更新按钮添加到按钮面板
        buttonPanel.add(cancelButton); // 将取消按钮添加到按钮面板

        dialog.add(formPanel, BorderLayout.CENTER); // 将表单面板添加到对话框中央
        dialog.add(buttonPanel, BorderLayout.SOUTH); // 将按钮面板添加到对话框底部

        dialog.setVisible( true); // 显示对话框
    }

    /**
     * 更新连接状态显示
     * 根据Socket客户端连接状态更新UI显示
     */
    private void updateConnectionStatus() { // 更新连接状态的方法
        if(socketClient.isConnected()){
            connectionStatusLable.setText("已连接"); // 设置连接状态标签为"已连接"
            connectionStatusLable.setForeground(Color.GREEN); // 设置连接状态标签为绿色
            connectButton.setEnabled(false); // 禁用连接按钮
        } else {
            connectionStatusLable.setText("未连接");
            connectionStatusLable.setForeground(Color.RED);
            connectButton.setEnabled(true); // 启用连接按钮
        }
    }

    /**
     * 更新状态栏信息
     * 在状态栏显示指定的消息
     * @param message 状态消息
     */
    private void updateStatus(String message) { // 更新状态栏信息的方法
        statusLabel.setText(message); // 设置状态标签的文本内容
    }

    /**
     * 主方法，启动客户端GUI
     * 程序的入口点，初始化并显示客户端图形界面
     * @param args 命令行参数
     */
    public static void main(String[] args) { // 主方法，程序入口点
        // 设置系统外观
        try { // 尝试设置系统外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // 设置为系统默认的外观和感觉
        } catch (Exception e) { // 如果设置失败
            System.err.println("设置外观失败: " + e.getMessage()); // 输出错误信息到标准错误流
        }

        // 在事件调度线程中创建和显示GUI
        SwingUtilities.invokeLater(new Runnable() { // 在Swing事件调度线程中执行GUI创建
            @Override
            public void run() { // 重写run方法
                new ClientGUI().setVisible(true); // 创建ClientGUI实例并设置为可见
            }
        });
    } // main方法结束

}
