CREATE TABLE IF NOT EXISTS staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100),
    permission_level VARCHAR(20) DEFAULT 'worker'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    token_balance INT DEFAULT 0,
    vip_level VARCHAR(20) DEFAULT '普通会员',
    accumulated_spend DECIMAL(10, 2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS token_packages (
    package_id INT AUTO_INCREMENT PRIMARY KEY,
    package_name VARCHAR(100) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    token_count INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS machines (
    machine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    tokens_per_game INT DEFAULT 1,
    status VARCHAR(20) DEFAULT 'available',
    staff_id INT,
    CONSTRAINT fk_machines_staff FOREIGN KEY (staff_id) REFERENCES staff(staff_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS token_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    package_id INT,
    transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    amount_paid DECIMAL(10, 2) NOT NULL,
    tokens_purchased INT NOT NULL,
    CONSTRAINT fk_trans_member FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT fk_trans_package FOREIGN KEY (package_id) REFERENCES token_packages(package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS game_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT NOT NULL,
    machine_id INT NOT NULL,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    token_consumed INT,
    score INT,
    CONSTRAINT fk_sess_member FOREIGN KEY (member_id) REFERENCES members(member_id),
    CONSTRAINT fk_sess_machine FOREIGN KEY (machine_id) REFERENCES machines(machine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
