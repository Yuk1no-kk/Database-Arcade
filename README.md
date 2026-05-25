# 游戏厅管理系统（Arcade Management System）

> 数据库课程作业 —— 基于 Web 的游戏厅综合管理平台

## 一、项目概述

本项目是一个完整的 Web 数据库应用，用于管理游戏厅日常运营的核心业务：会员管理、游戏机管理、代币充值消费。前端为纯 HTML/CSS/JS 单页面应用，后端为 Spring Boot RESTful API，数据库使用 MySQL。

**交互方式**：鼠标点击完成所有按钮操作，键盘在搜索框中输入后按 Enter 触发搜索，表单弹窗内可用 Tab 键在字段间跳转。

## 二、技术栈

| 层级 | 技术 |
|------|------|
| 前端 | HTML5 + CSS3 + Vanilla JavaScript（无框架） |
| 后端 | Spring Boot 3.2.5 + JDBC（JdbcTemplate） |
| 数据库 | MySQL 8.0 |
| 构建 | Maven |
| 通信 | RESTful JSON API |

## 三、项目结构

```
ArcadeSystem/
├── pom.xml                                    # Maven 配置
├── README.md                                  # 本文档
├── docs/superpowers/specs/
│   └── 2026-05-25-arcade-management-system-design.md  # 设计规格文档
├── src/main/resources/
│   ├── application.properties                 # 数据库连接 & 服务端口配置
│   └── static/
│       ├── index.html                         # 唯一前端页面（3个Tab）
│       ├── css/style.css                      # 全局样式
│       └── js/
│           ├── api.js                         # AJAX 封装 + 统一错误处理
│           ├── app.js                         # Tab切换 + 模态框 + 初始化
│           ├── member.js                      # 会员管理模块
│           ├── machine.js                     # 游戏机管理模块
│           └── transaction.js                 # 充值消费模块
└── src/main/java/com/example/arcadesystem/
    ├── ArcadeApplication.java                 # Spring Boot 启动入口
    ├── DatabaseSetup.java                     # 数据库建表脚本（独立运行）
    ├── config/
    │   └── CorsConfig.java                    # CORS 跨域配置
    ├── controller/
    │   ├── MemberController.java              # 会员 API（5个端点）
    │   ├── MachineController.java             # 游戏机 API（5个端点）
    │   └── TransactionController.java         # 交易 API（4个端点）
    ├── service/
    │   ├── MemberService.java                 # 会员业务逻辑
    │   ├── MachineService.java                # 游戏机业务逻辑
    │   └── TransactionService.java            # 充值消费业务逻辑（含事务）
    ├── dao/
    │   ├── MemberDao.java                     # 会员数据访问（JDBC）
    │   ├── MachineDao.java                    # 游戏机数据访问（JDBC）
    │   └── TransactionDao.java                # 交易数据访问（JDBC）
    ├── model/
    │   ├── Member.java                        # 会员实体
    │   ├── Machine.java                       # 游戏机实体
    │   ├── TokenPackage.java                  # 代币套餐实体
    │   └── TokenTransaction.java              # 交易记录实体
    ├── dto/
    │   └── ApiResponse.java                   # 统一响应格式
    └── exception/
        ├── GlobalExceptionHandler.java        # 全局异常拦截器
        ├── BusinessException.java             # 业务异常（如余额不足）
        └── NotFoundException.java             # 资源不存在异常
```

## 四、架构分层

```
┌──────────────────────────────────────────────────────────┐
│                     浏览器 (index.html)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐            │
│  │ member.js │  │machine.js│  │transaction.js│            │
│  └─────┬─────┘  └─────┬────┘  └──────┬───────┘            │
│        └───────────────┼──────────────┘                    │
│                        │  fetch()                          │
│                   ┌────┴────┐                              │
│                   │ api.js  │  统一错误处理 + toast 提示     │
│                   └────┬────┘                              │
└────────────────────────┼──────────────────────────────────┘
                         │  HTTP JSON
┌────────────────────────┼──────────────────────────────────┐
│              Spring Boot 后端                              │
│                        │                                   │
│   ┌────────────────────┼────────────────────┐              │
│   │           Controller 层                   │              │
│   │  (接收请求, 参数绑定, 返回 JSON)           │              │
│   └────────────────────┬────────────────────┘              │
│                        │                                   │
│   ┌────────────────────┼────────────────────┐              │
│   │            Service 层                     │              │
│   │  (业务校验, @Transactional 事务管理)       │              │
│   └────────────────────┬────────────────────┘              │
│                        │                                   │
│   ┌────────────────────┼────────────────────┐              │
│   │             Dao 层                        │              │
│   │  (纯 JDBC/JdbcTemplate, 手写 SQL)         │              │
│   └────────────────────┬────────────────────┘              │
│                        │                                   │
│   ┌──────────┐  ┌──────┴──────┐                           │
│   │Exception │  │  Exception   │  统一异常处理 → 错误 JSON  │
│   │ Handler  │  │  类体系      │                           │
│   └──────────┘  └─────────────┘                           │
└────────────────────────┬──────────────────────────────────┘
                         │  JDBC
┌────────────────────────┼──────────────────────────────────┐
│                   MySQL (arcade_db)                        │
│  ┌─────────────┐  ┌─────────────┐  ┌──────────────────┐   │
│  │   members   │  │  machines   │  │token_transactions│   │
│  └─────────────┘  └─────────────┘  └──────────────────┘   │
│  ┌──────────┐  ┌──────────────┐  ┌─────────────────┐      │
│  │  staff   │  │token_packages│  │  game_sessions  │      │
│  └──────────┘  └──────────────┘  └─────────────────┘      │
└──────────────────────────────────────────────────────────┘
```

## 五、数据库表结构

共 6 张表（维护已有的完整 schema）：

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `members` | 会员 | member_id, name, phone, token_balance, vip_level, accumulated_spend |
| `machines` | 游戏机 | machine_id, name, type, tokens_per_game, status, staff_id(FK) |
| `token_packages` | 代币套餐 | package_id, package_name, price, token_count |
| `token_transactions` | 充值记录 | transaction_id, member_id(FK), package_id(FK), transaction_date, amount_paid, tokens_purchased |
| `game_sessions` | 游玩记录 | session_id, member_id(FK), machine_id(FK), start_time, end_time, token_consumed, score |
| `staff` | 管理员 | staff_id, username, password, name, permission_level |

## 六、API 接口文档

所有接口返回统一格式：`{ code: 200, message: "ok", data: ... }`

### 6.1 会员管理

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/members` | ?keyword=&sort=&order=&page=&size= | 列表查询（搜索+排序+分页） |
| GET | `/api/members/{id}` | - | 查询单个会员 |
| POST | `/api/members` | JSON body | 新增会员（name 必填） |
| PUT | `/api/members/{id}` | JSON body | 编辑会员 |
| DELETE | `/api/members/{id}` | - | 删除（存在交易记录则拒绝） |

**支持的排序字段**：`member_id`, `name`, `vipLevel`, `tokenBalance`, `accumulatedSpend`

### 6.2 游戏机管理

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/machines` | ?keyword=&sort=&order=&page=&size= | 列表查询 |
| GET | `/api/machines/{id}` | - | 查询单个 |
| POST | `/api/machines` | JSON body | 新增（name 必填） |
| PUT | `/api/machines/{id}` | JSON body | 编辑 |
| DELETE | `/api/machines/{id}` | - | 删除（存在游玩记录则拒绝） |

### 6.3 套餐 & 交易

| 方法 | 路径 | 参数 | 说明 |
|------|------|------|------|
| GET | `/api/packages` | - | 获取所有套餐列表 |
| POST | `/api/transactions/recharge` | {memberId, packageId} | 充值（更新余额+累计消费） |
| POST | `/api/transactions/consume` | {memberId, machineId, tokens} | 消费扣币（余额不足则拒绝） |
| GET | `/api/transactions` | ?page=&size= | 交易记录列表 |

## 七、异常处理

| 场景 | HTTP 状态码 | code | message 示例 |
|------|------------|------|-------------|
| 资源不存在 | 404 | 404 | "会员不存在" / "游戏机不存在" |
| 余额不足 | 400 | 400 | "余额不足，无法消费" |
| 字段校验失败 | 400 | 400 | "姓名不能为空" |
| 删除有关联数据 | 400 | 400 | "该会员存在交易记录，无法删除" |
| 服务器内部错误 | 500 | 500 | "服务器内部错误" |

前端 `api.js` 自动检查 code，非 200 时弹出红色 toast 提示，无需各模块重复处理。

## 八、数据库建表

首次使用前需执行建表脚本：

```bash
# 方式一：用 IDE 直接运行 DatabaseSetup.java 的 main 方法

# 方式二：手动连接 MySQL 执行（SQL 在 DatabaseSetup.java 中）
```

数据库连接信息在 `src/main/resources/application.properties` 中配置，默认值：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/arcade_db?serverTimezone=UTC&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=20060507
server.port=8080
```

## 九、启动运行

```bash
# 1. 确保 MySQL 服务已启动，且 arcade_db 数据库已建表

# 2. 启动后端
./mvnw spring-boot:run

# 3. 浏览器打开
# http://localhost:8080
```

## 十、各文件职责速查

### 后端

| 文件 | 职责 |
|------|------|
| `ArcadeApplication.java` | Spring Boot 入口，启用事务管理 |
| `CorsConfig.java` | 允许前端跨域请求（开发时前后端分离） |
| `MemberController.java` | 会员 REST API，接收请求、校验参数、调用 Service |
| `MachineController.java` | 游戏机 REST API |
| `TransactionController.java` | 套餐查询 + 充值 + 消费 + 交易记录 API |
| `MemberService.java` | 会员业务校验：姓名非空、存在性检查、删除前关联检查 |
| `MachineService.java` | 游戏机业务校验：名称非空、删除前游玩记录检查 |
| `TransactionService.java` | 充值/消费核心逻辑，@Transactional 保证数据一致性 |
| `MemberDao.java` | 会员表 JDBC 操作：动态 SQL 拼接、搜索、排序、分页 |
| `MachineDao.java` | 游戏机表 JDBC 操作 |
| `TransactionDao.java` | 交易表 + 套餐表 + 游玩记录表 JDBC 操作 |
| `ApiResponse.java` | 统一响应 DTO，静态工厂方法 `ok()` / `fail()` |
| `BusinessException.java` | 业务异常，携带自定义 code 和 message |
| `NotFoundException.java` | 资源不存在异常，HTTP 404 |
| `GlobalExceptionHandler.java` | @RestControllerAdvice 全局拦截，异常→JSON 响应 |
| `DatabaseSetup.java` | 独立运行的建表脚本，DROP + CREATE 全部 6 张表 |

### 前端

| 文件 | 职责 |
|------|------|
| `index.html` | 页面骨架：顶部标题、3 个 Tab 按钮、3 个内容区、模态框 |
| `style.css` | 全局样式：表格、按钮、表单、模态框、排序指示器、状态标签、toast |
| `api.js` | 封装 fetch：`API.get()` / `.post()` / `.put()` / `.del()`，自动解析 `ApiResponse`，非 200 时弹 toast 并抛异常 |
| `app.js` | Tab 切换事件、模态框显示/隐藏/提交逻辑、页面初始化 |
| `member.js` | 会员模块：`MemberModule` 对象封装搜索、排序、翻页、新增弹窗、编辑弹窗、删除确认 |
| `machine.js` | 游戏机模块：同上结构，支持状态标签着色（可用=绿，维护中=橙） |
| `transaction.js` | 交易模块：充值弹窗（选会员+套餐）、消费弹窗（选会员+机器+代币数）、交易列表 |

## 十一、前端交互说明

- **鼠标操作**：所有按钮、Tab、表头排序、分页均通过鼠标点击操作
- **键盘操作**：搜索框输入关键词后按 Enter 触发搜索；弹窗表单内按 Tab 在字段间跳转
- **无快捷键依赖**：不依赖任何键盘快捷键，纯鼠标也可完成所有操作
