# Arcade Management System Design Spec

**Date**: 2026-05-25
**Status**: Approved

## 1. Overview

Transform the existing JavaFX desktop application into a full-stack web-based arcade (game hall) management system. Staff can manage members, machines, and token transactions entirely through a browser using mouse and keyboard (no keyboard shortcuts required).

## 2. Tech Stack

- **Frontend**: HTML + CSS + Vanilla JavaScript (single page, Tab switching)
- **Backend**: Spring Boot + JDBC (RESTful JSON API)
- **Database**: MySQL 8.0 (existing `arcade_db` schema, no changes needed)
- **Build**: Maven

## 3. Backend Architecture

### 3.1 Package Structure

```
src/main/java/com/example/arcadesystem/
├── ArcadeApplication.java          # Spring Boot entry
├── config/
│   └── CorsConfig.java             # CORS for frontend dev
├── controller/
│   ├── MemberController.java
│   ├── MachineController.java
│   └── TransactionController.java
├── service/
│   ├── MemberService.java
│   ├── MachineService.java
│   └── TransactionService.java
├── dao/
│   ├── MemberDao.java
│   ├── MachineDao.java
│   └── TransactionDao.java
├── model/
│   ├── Member.java
│   ├── Machine.java
│   ├── TokenPackage.java
│   └── TokenTransaction.java
├── dto/
│   └── ApiResponse.java            # {code, message, data}
└── exception/
    ├── GlobalExceptionHandler.java  # @RestControllerAdvice
    ├── BusinessException.java       # e.g. insufficient balance
    └── NotFoundException.java       # e.g. member not found
```

### 3.2 Layer Responsibilities

- **Controller**: HTTP request handling, parameter validation, returns `ApiResponse`
- **Service**: Business logic (balance deduction, VIP recalculation, cascade checks)
- **Dao**: Pure JDBC, hand-written SQL, returns model objects
- **Model**: POJOs matching database tables
- **Exception**: Unified error handling via `GlobalExceptionHandler`

### 3.3 API Endpoints

| Method | Path | Params | Description |
|--------|------|--------|-------------|
| GET | `/api/members` | keyword, sort, order, page, size | List + search + sort + paginate |
| POST | `/api/members` | body: Member JSON | Create member |
| PUT | `/api/members/{id}` | body: Member JSON | Update member |
| DELETE | `/api/members/{id}` | - | Delete (blocks if has transactions) |
| GET | `/api/machines` | keyword, sort, order, page, size | List + search + sort + paginate |
| POST | `/api/machines` | body: Machine JSON | Create machine |
| PUT | `/api/machines/{id}` | body: Machine JSON | Update machine |
| DELETE | `/api/machines/{id}` | - | Delete (blocks if has sessions) |
| GET | `/api/packages` | - | List token packages |
| POST | `/api/transactions/recharge` | memberId, packageId | Recharge tokens |
| POST | `/api/transactions/consume` | memberId, machineId, tokens | Consume tokens |
| GET | `/api/transactions` | memberId, page, size | Transaction history |

### 3.4 Exception Mapping

| Scenario | HTTP Status | code | message |
|----------|------------|------|---------|
| Resource not found | 404 | 404 | "会员不存在" |
| Insufficient balance | 400 | 400 | "余额不足，无法消费" |
| Required field empty | 400 | 400 | "姓名不能为空" |
| Delete with dependencies | 400 | 400 | "该会员存在交易记录，无法删除" |
| Internal error | 500 | 500 | "服务器内部错误" |

### 3.5 Database

Keep the existing 6-table schema in `arcade_db`:

- `staff` — admins/workers
- `members` — arcade members (token_balance, vip_level, accumulated_spend)
- `token_packages` — predefined recharge packages
- `machines` — arcade machines (type, tokens_per_game, status)
- `token_transactions` — recharge & consume records
- `game_sessions` — play session logs

## 4. Frontend Architecture

### 4.1 File Structure

```
src/main/resources/static/
├── index.html
├── css/
│   └── style.css
└── js/
    ├── api.js           # Unified fetch wrapper + error handling
    ├── member.js        # Member module
    ├── machine.js       # Machine module
    └── transaction.js   # Transaction module
```

### 4.2 Page Layout (Single Page, 3 Tabs)

```
┌─────────────────────────────────────────────┐
│  🎮  Arcade Management System               │
│  [ Members ] [ Machines ] [ Transactions ]   │  ← Tabs
│ ┌───────────────────────────────────────────┐│
│ │ 🔍 [search input...]  [+ New]            ││  ← Toolbar
│ ├───────────────────────────────────────────┤│
│ │ Name ▲ | Phone  | VIP     | Balance | ... ││  ← Sortable headers
│ │ Alice  | 138... | Gold    | 500     | ... ││
│ │ Bob    | 139... | Silver  | 200     | ... ││
│ ├───────────────────────────────────────────┤│
│ │          ◀ 1  2  3 ... ▶                 ││  ← Pagination
│ └───────────────────────────────────────────┘│
└─────────────────────────────────────────────┘
```

### 4.3 Interaction Design

- **Mouse**: Tab switching, button clicks, sort by clicking headers, pagination, form input
- **Keyboard**: Type in search box + Enter to trigger search; Tab between form fields in modals
- **Modal dialogs** for create/edit forms
- **Confirm dialog** before delete
- Click sort header once = ascending, again = descending, third = no sort

### 4.4 Module Details

#### Member Management
- Search by name or phone (fuzzy)
- Sort by name, VIP level, balance, accumulated spend
- CRUD with name required
- Delete blocked when member has transaction records

#### Machine Management
- Search by name or type
- Sort by name, type, tokens per game
- CRUD with name required
- Status: available / in_maintenance
- Delete blocked when machine has game sessions

#### Transactions
- Recharge: select member + package → auto-calculates tokens → updates balance
- Consume: select member + machine + token count → deducts balance (throws if insufficient)
- Transaction history list with pagination

### 4.5 api.js Error Handling

All fetch calls go through `api.js` which:
- Sets JSON headers
- Parses `ApiResponse`
- If `code !== 200`, displays error message via alert/toast
- Returns `data` on success

### 4.6 CSS Design

- Clean, minimal style — light background, card-based layout
- Responsive table with hover rows
- Modal overlay with centered form
- Color-coded status badges (available = green, maintenance = orange)

## 5. Existing Code to Modify

| File | Action |
|------|--------|
| `pom.xml` | Add Spring Boot starter dependencies, remove JavaFX dependencies |
| `HelloApplication.java` | Replace with `ArcadeApplication.java` (Spring Boot entry) |
| `HelloController.java` | Delete, replaced by REST controllers |
| `Launcher.java` | Delete, Spring Boot has its own main |
| `hello-view.fxml` | Delete, replaced by `static/index.html` |
| `DatabaseSetup.java` | Keep as reference, logic moved to Dao layer |
| `module-info.java` | Delete, not needed for Spring Boot |

## 6. Non-Goals (Explicitly Out of Scope)

- User authentication / login system (deferred)
- Keyboard shortcuts (user explicitly said no)
- Data visualization / charts / reports (deferred)
- Staff management CRUD (deferred)
- Game session tracking UI (deferred, DB table exists for future use)
