package com.example.arcadesystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        // 1. 数据库连接配置
        String url = "jdbc:mysql://localhost:3306/arcade_db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true";
        String user = "root";
        String password = "20060507";

        // 2. 用try-with-resources语法，自动关闭连接/语句，避免资源泄漏
        try (
                // 建立数据库连接
                Connection conn = DriverManager.getConnection(url, user, password);
                // 创建SQL执行对象
                Statement stmt = conn.createStatement();
        ) {
            System.out.println("太棒了组长，成功连接到数据库！");

            // 3. 示例：创建会员表（你可以在这里写你的建表语句）
            String createMemberTable = """
                CREATE TABLE IF NOT EXISTS member (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    name VARCHAR(50) NOT NULL,
                    phone VARCHAR(20) UNIQUE NOT NULL,
                    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
                """;
            stmt.executeUpdate(createMemberTable);
            System.out.println("会员表创建成功！");

        } catch (Exception e) {

            e.printStackTrace();
            System.out.println("数据库操作失败：" + e.getMessage());
        }
    }
}