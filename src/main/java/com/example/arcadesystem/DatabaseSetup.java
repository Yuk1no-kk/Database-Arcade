package com.example.arcadesystem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DatabaseSetup {
    public static void main(String[] args) {
        // 1. 数据库连接配置
        String url = "jdbc:mysql://localhost:3306/arcade_db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true&allowMultiQueries=true";
        String user = "root";
        String password = "20060507";

        try {
            // 2. 建立连接
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("成功连接到数据库！正在全自动生成所有游戏厅表结构...");
            Statement stmt = conn.createStatement();

            // 3. 准备完整的建表逻辑 (使用 allowMultiQueries 一次执行多条语句)
            String completeSql =
                    // --- A. 先删表 (为了重置系统，防止报错。按照孩子->父母的顺序删) ---
                    "DROP TABLE IF EXISTS game_sessions;" +
                            "DROP TABLE IF EXISTS token_transactions;" +
                            "DROP TABLE IF EXISTS machines;" +
                            "DROP TABLE IF EXISTS token_packages;" +
                            "DROP TABLE IF EXISTS members;" +
                            "DROP TABLE IF EXISTS staff;" +

                            // --- B. 再建表 (按照无外键父母->有外键孩子的顺序建) ---

                            // B1. 管理员表 (Parents - 无外键)
                            "CREATE TABLE staff (" +
                            "  staff_id INT AUTO_INCREMENT PRIMARY KEY," + // 管理员ID，主键
                            "  username VARCHAR(50) NOT NULL UNIQUE," +  // 账号，必须有，不能重复
                            "  password VARCHAR(255) NOT NULL," +        // 密码
                            "  name VARCHAR(100)," +
                            "  permission_level VARCHAR(20) DEFAULT 'worker'" + // 权限：admin/worker
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" +

                            // B2. 会员表 (Parents - 无外键，比上次多了累计消费)
                            "CREATE TABLE members (" +
                            "  member_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  name VARCHAR(50) NOT NULL," +
                            "  phone VARCHAR(20)," +
                            "  token_balance INT DEFAULT 0," +               // 代币余额
                            "  vip_level VARCHAR(20) DEFAULT '普通会员'," + // 金卡/银卡/普通
                            "  accumulated_spend DECIMAL(10, 2) DEFAULT 0.00" + // 累计消费金额，用于算VIP
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" +

                            // B3. 代币套餐表 (Parents - 无外键)
                            "CREATE TABLE token_packages (" +
                            "  package_id INT AUTO_INCREMENT PRIMARY KEY," + // 套餐ID
                            "  package_name VARCHAR(100) NOT NULL," +        // 名称：如100元套餐
                            "  price DECIMAL(10, 2) NOT NULL," +            // 价格
                            "  token_count INT NOT NULL" +                  // 包含代币
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" +

                            // B4. 游戏机表 (Child of staff)
                            "CREATE TABLE machines (" +
                            "  machine_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  name VARCHAR(100) NOT NULL," +
                            "  type VARCHAR(50)," + // 赛车/射击
                            "  tokens_per_game INT DEFAULT 1," + // 单次耗币
                            "  status VARCHAR(20) DEFAULT 'available'," + // 状态：available/in_maintenance
                            "  staff_id INT," + // 👉 这里连接管理员表
                            "  CONSTRAINT fk_machines_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id)" + // 外键约束
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" +

                            // B5. 代币交易记录表 (Junction of member, token_package)
                            "CREATE TABLE token_transactions (" +
                            "  transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  member_id INT NOT NULL," + // 👉 这里连接会员表
                            "  package_id INT NOT NULL," + // 👉 这里连接套餐表
                            "  transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP," + // 交易时间，默认现在
                            "  amount_paid DECIMAL(10, 2) NOT NULL," +
                            "  tokens_purchased INT NOT NULL," +
                            "  CONSTRAINT fk_trans_member FOREIGN KEY (member_id) REFERENCES members(member_id)," +
                            "  CONSTRAINT fk_trans_package FOREIGN KEY (package_id) REFERENCES token_packages(package_id)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;" +

                            // B6. 游玩记录表 (Junction of member, machine)
                            "CREATE TABLE game_sessions (" +
                            "  session_id INT AUTO_INCREMENT PRIMARY KEY," +
                            "  member_id INT NOT NULL," + // 👉 这里连接会员表
                            "  machine_id INT NOT NULL," + // 👉 这里连接机器表
                            "  start_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                            "  end_time DATETIME," +
                            "  token_consumed INT," + // 实际扣币（可以算VIP打折）
                            "  score INT," +
                            "  CONSTRAINT fk_sess_member FOREIGN KEY (member_id) REFERENCES members(member_id)," +
                            "  CONSTRAINT fk_sess_machine FOREIGN KEY (machine_id) REFERENCES machines(machine_id)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

            // 4. 让 Java 把全套图纸交给 MySQL 去执行
            stmt.executeUpdate(completeSql);
            System.out.println("大功告成！组长牛逼，所有表结构都已经完美建好啦！！！可以截图去发邮件汇报啦！🎉🎉🎉");

            // 5. 关门走人
            stmt.close();
            conn.close();

        } catch (Exception e) {
            System.out.println("哎呀，报错了，请检查一下密码对不对：");
            e.printStackTrace();
        }
    }
}