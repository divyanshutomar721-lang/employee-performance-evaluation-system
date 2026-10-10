# Employee Performance Evaluation System (EPES)

A web-based system for managing employee performance evaluations.
Managers evaluate employees, set goals, give feedback, manage teams and generate reports.
Employees track their progress, view feedback, set personal goals and view their own reports.

## Technology Stack

| Layer      | Technology                                            |
|------------|-------------------------------------------------------|
| Language   | Java 17+ (compiled with `--release 17`)               |
| Build      | Apache Maven (WAR packaging)                          |
| Web        | Jakarta Servlet 6.0, JSP, JSTL (Tomcat 10.1)         |
| Database   | PostgreSQL (plain JDBC, DAO pattern, transactions)    |
| Frontend   | JSP + Bootstrap 5 (CDN) + Bootstrap Icons + CSS3      |
| Logging    | `java.util.logging`                                   |
| Testing    | JUnit 5 (Jupiter), Maven Surefire                     |

## Prerequisites

- **JDK 17 or later** (JDK 24 tested)
- **Apache Maven 3.9+**
- **PostgreSQL** (running, with a database called `epes` created)

## Setup

1. **Create the database** (if not already done):
   ```sql
   CREATE DATABASE epes;
   ```

2. **Set environment variables**:
   ```bash
   # Linux / macOS
   export DB_PASSWORD=your_password
   export ADMIN_EMAIL=admin@epes.local           # optional bootstrap admin
   export TEMP_LOGIN_ENABLED=true                # optional (default: true)
   export GOAL_CHECK_INTERVAL_MINUTES=60         # optional (default: 60)

   # Windows (PowerShell)
   $env:DB_PASSWORD = "your_password"
   $env:ADMIN_EMAIL = "admin@epes.local"
   $env:TEMP_LOGIN_ENABLED = "true"
   $env:GOAL_CHECK_INTERVAL_MINUTES = "60"
   ```
   Optionally set `DB_URL` (default `jdbc:postgresql://localhost:5432/epes`) and `DB_USER` (default `postgres`).

3. **Build and run**:
   ```bash
   mvn clean package
   mvn cargo:run
   ```

4. Open your browser:
   - **Application**: [http://localhost:8080/epes/](http://localhost:8080/epes/)
   - **Health check**: [http://localhost:8080/epes/health](http://localhost:8080/epes/health)

## Security Architecture

The application enforces a multi-layered defence-in-depth architecture across filter, servlet, and service layers:

### 1. Filter Pipeline (Order of Execution)
1. **`EncodingFilter`** (`/*`): Forces UTF-8 character encoding on all requests and responses.
2. **`SecurityHeadersFilter`** (`/*`): Injects standard security headers (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: same-origin`) and applies `Cache-Control: no-store` on non-asset requests.
3. **`AuthenticationFilter`** (`/*`): Blocks unauthenticated access to protected endpoints, redirecting visitors to `/login` without creating dummy sessions.
4. **`RoleFilter`** (`/*`): Enforces role access by URL prefix (`/admin/*`, `/manager/*`, `/employee/*`), returning HTTP 403 on authorization mismatch.
5. **`CsrfFilter`** (`/*`): Validates cryptographically secure `_csrf` tokens on every `POST` request (including login and logout) using constant-time comparison (`MessageDigest.isEqual`), rejecting expired or missing tokens with HTTP 403.

### 2. Session Management & Fixation Defense
- **Session Timeout**: 30 minutes, cookie-tracked with `HttpOnly` enabled.
- **Session Fixation Defense**: On successful authentication (`LoginServlet`), the existing session is invalidated and replaced with a newly generated session and a fresh CSRF token.
- **Session Invalidation**: `LogoutServlet` completely invalidates the session upon POST logout.

### 3. Role Access Table & Application Endpoints

| URL Pattern | Method | Permitted Roles | Description |
|---|:---:|:---:|---|
| `/` | GET | Anonymous / Any | Public landing page (redirects to `/home` if already logged in) |
| `/login` | GET, POST | Anonymous | Login page with CSRF token; POST authenticates and renews session |
| `/logout` | POST | Authenticated | Invalidates session and redirects to `/login` with flash alert |
| `/home` | GET | Authenticated | Dispatches user to role-specific dashboard |
| `/admin/dashboard` | GET | `ADMIN` | Admin dashboard shell & system governance |
| `/manager/dashboard` | GET | `MANAGER` | Manager dashboard shell & team performance |
| `/employee/dashboard` | GET | `EMPLOYEE` | Employee dashboard shell & personal goal tracking |
| `/health` | GET | Anonymous / Any | Health check confirming database connectivity |
| `/error` | GET, POST | Anonymous / Any | Central error handler rendering safe error views |
| `/assets/*` | GET | Anonymous / Any | Static CSS and JavaScript assets |

## Service Layer and Business Rules

The service layer encapsulates validation, authorization, atomic transactions, and business logic:

| Action / Entity | Admin | Manager | Employee | Key Rules Enforced |
|---|:---:|:---:|:---:|---|
| **User Management** | Create, Role, Active | - | View Self | Self-demotion & last active admin protected; emails unique. |
| **Team Management** | View All | Create, Edit, Delete (Own) | View (Member of) | Only active `EMPLOYEE`s can be team members; multi-team allowed. |
| **Criteria Setup** | - | Create, Edit, Deactivate (Own) | - | Weights 1..100; unique name per manager. |
| **Evaluations** | View All | Create, Delete (Own supervised) | View (Own) | Employee must be in manager's team; snapshots weights & names; weighted scoring formula; 1 per period. |
| **Goal Tracking** | View All | Assign, Edit, Progress (Own) | Create, Edit, Progress (Self) | Progress auto-maps status (0=NOT_STARTED, 1..99=IN_PROGRESS, 100=COMPLETED); past dates rejected. |
| **Feedback** | View All | Give, Delete (Supervised) | View (Own) | Continuous 360 feedback with 1..5 star ratings. |
| **Analytics & Reports** | View All | Team & Overview (Managed) | Employee Report (Self) | Pure aggregations: trajectory trend detection, stage distribution, average scores. |

### Performance Tiers (Rocket Stages)
- **LAUNCHPAD**: Score < 40.00
- **LIFTOFF**: Score 40.00 to < 60.00
- **ORBIT**: Score 60.00 to < 75.00
- **CRUISE**: Score 75.00 to < 90.00
- **SUMMIT**: Score &ge; 90.00

## Concurrency & Background Execution

- **`GoalDeadlineScheduler`**: Single daemon thread (`"epes-goal-deadline"`) running on a configurable interval (`GOAL_CHECK_INTERVAL_MINUTES`). Automatically detects past-deadline incomplete goals and transitions them to `MISSED` in an atomic transaction.
- **`ReportJobService`**: Fixed pool of 2 daemon threads (`"epes-report-N"`) managing asynchronous report generation jobs with status polling (`PENDING`, `RUNNING`, `DONE`, `FAILED`) and LRU history eviction.

## Database

The PostgreSQL schema is located at `src/main/resources/db/schema.sql` and is auto-initialized on application startup via `SchemaInitializer`.
The schema contains 8 core tables: `users`, `teams`, `team_members`, `criteria`, `evaluations`, `evaluation_scores`, `goals`, `feedback`.
Full data dictionary and text ER diagrams are documented in [db/README.md](db/README.md).

## Running Tests

Unit and integration tests run via Maven Surefire:
```bash
mvn test
```

## Project Structure

```
src/main/java/com/epes/
  concurrent/   — GoalDeadlineScheduler, ReportJobService, JobStatus
  config/       — DatabaseConfig, AppConstants, AppContext
  dao/          — DAO interfaces (UserDao, TeamDao, CriterionDao, EvaluationDao, GoalDao, FeedbackDao)
  dao/impl/     — Plain JDBC DAO implementations
  exception/    — ServiceException hierarchy (Validation, Auth, BusinessRule) & DataAccessException
  filter/       — AccessPolicy, EncodingFilter, SecurityHeadersFilter, AuthenticationFilter, RoleFilter, CsrfFilter
  listener/     — AppStartupListener (schema init, admin bootstrap, AppContext lifecycle)
  model/        — Domain objects (User, Admin, Manager, Employee, Team, Criterion, Evaluation, Goal, Feedback, RocketStage)
  service/      — Service interfaces, ScoreCalculator, GoalProgressRule
  service/impl/ — Service implementations
  service/report/ — EmployeeReport, TeamReport, ManagerOverview, ReportAggregator, Trend
  servlet/      — BaseServlet, HomeServlet, LoginServlet, LogoutServlet, AdminDashboardServlet, ManagerDashboardServlet, EmployeeDashboardServlet, ErrorServlet, HealthServlet
  util/         — ConnectionFactory, TransactionManager, SchemaInitializer, Validator, CsrfTokens, CsrfSupport, Flash, FlashMessage

src/main/resources/
  db/schema.sql — Idempotent PostgreSQL DDL script

src/main/webapp/
  index.jsp
  assets/css/style.css
  WEB-INF/web.xml
  WEB-INF/views/
    login.jsp
    error.jsp
    admin.jsp
    manager.jsp
    employee.jsp
    fragments/
      header.jspf
      footer.jspf

src/test/java/com/epes/
  concurrent/   — GoalDeadlineSchedulerTest, ReportJobServiceTest
  dao/          — BaseDaoTest, UserDaoTest, TeamDaoTest, CriterionDaoTest, EvaluationDaoTest, GoalDaoTest, FeedbackDaoTest
  filter/       — AccessPolicyTest
  model/        — RocketStageTest
  service/      — BaseServiceTest, ScoreCalculatorTest, GoalProgressRuleTest, AuthServiceTest, UserServiceTest, TeamServiceTest, CriterionServiceTest, EvaluationServiceTest, GoalServiceTest, FeedbackServiceTest, ReportServiceTest
  service/report/ — ReportAggregatorTest
  util/         — ValidatorTest, TransactionRollbackTest, CsrfTokensTest, FlashTest

db/             — Database documentation & data dictionary (README.md)
docs/           — College project documentation
```

## Project Status

- [x] **Phase 0** — Maven WAR skeleton, config, health check
- [x] **Phase 1** — Database schema, domain model (OOP), JDBC DAOs, transaction support
- [x] **Phase 2** — Services, validation, scoring, reports, concurrency
- [x] **Phase 3** — Authentication, authorization filters, CSRF protection, error handling, dashboard shells
- [ ] Phase 4 — JSP views, dashboards, Bootstrap 5 UI & charts
