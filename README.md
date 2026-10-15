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
| `/admin/dashboard` | GET | `ADMIN` | Admin dashboard with live user count metrics |
| `/admin/users` | GET | `ADMIN` | User directory and user creation form |
| `/admin/users/create` | POST | `ADMIN` | Creates a new user (ADMIN, MANAGER, EMPLOYEE) |
| `/admin/users/role` | POST | `ADMIN` | Updates a user's system role |
| `/admin/users/active` | POST | `ADMIN` | Activates or deactivates a user account |
| `/manager/dashboard` | GET | `MANAGER` | Manager dashboard with live team and criteria metrics |
| `/manager/criteria` | GET | `MANAGER` | Evaluation criteria list, active weight summary, and creation form |
| `/manager/criteria/save` | POST | `MANAGER` | Creates or updates an evaluation criterion |
| `/manager/criteria/deactivate`| POST | `MANAGER` | Deactivates a criterion |
| `/manager/criteria/delete` | POST | `MANAGER` | Permanently deletes a criterion |
| `/manager/teams` | GET | `MANAGER` | Managed teams overview and new team form with member picker |
| `/manager/teams/create` | POST | `MANAGER` | Creates a new team with member assignments in one transaction |
| `/manager/teams/view` | GET | `MANAGER` | Team detail page, member roster, role updates, and member add |
| `/manager/teams/update` | POST | `MANAGER` | Updates team name and description |
| `/manager/teams/delete` | POST | `MANAGER` | Deletes a team and its member associations |
| `/manager/teams/members/add` | POST | `MANAGER` | Adds an active employee to a team |
| `/manager/teams/members/role` | POST | `MANAGER` | Updates a member's role in a team |
| `/manager/teams/members/remove`| POST | `MANAGER` | Removes a member from a team |
| `/manager/evaluations` | GET | `MANAGER` | Table of manager's evaluations and New evaluation button |
| `/manager/evaluations/new` | GET | `MANAGER` | Evaluation form with employee select, criteria ratings, and live score preview |
| `/manager/evaluations/create` | POST | `MANAGER` | Creates evaluation with score snapshots and calculates Rocket Stage |
| `/manager/evaluations/view` | GET | `MANAGER` | Evaluation detail view showing snapshot scores, stage badge, and delete button |
| `/manager/evaluations/delete` | POST | `MANAGER` | Deletes an evaluation |
| `/manager/goals` | GET | `MANAGER` | Table of assigned goals with progress update, inline edit, and assign form |
| `/manager/goals/assign` | POST | `MANAGER` | Assigns goal to an employee with future target date |
| `/manager/goals/progress` | POST | `MANAGER` | Updates progress percentage with automatic status mapping |
| `/manager/goals/edit` | POST | `MANAGER` | Edits goal description and target date |
| `/manager/goals/delete` | POST | `MANAGER` | Deletes an assigned goal |
| `/manager/feedback` | GET | `MANAGER` | Table of feedback given and Give feedback form |
| `/manager/feedback/create` | POST | `MANAGER` | Submits continuous feedback with 1..5 rating |
| `/manager/feedback/delete` | POST | `MANAGER` | Deletes a feedback record |
| `/employee/dashboard` | GET | `EMPLOYEE` | Employee dashboard shell & performance rocket stage |
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

## How to Use (Admin and Manager)

For detailed step-by-step instructions, see the [Phase 4A User Guide](docs/04-user-guide.md).

### Administrator Workflows
1. **Log in** with admin credentials. You will be redirected to `/admin/dashboard` showing live user counts across roles and account statuses.
2. Navigate to **Users** (`/admin/users`) to inspect all system accounts.
3. **Create Users**: Fill in email, full name, and select a system role (`ADMIN`, `MANAGER`, or `EMPLOYEE`).
4. **Change Role**: Select a new role from the dropdown for any user and click **Save**. Self-demotion and last-active-admin demotion are rejected.
5. **Activate / Deactivate**: Click **Deactivate** or **Activate** to control account status. Self-deactivation is strictly prohibited.

### Manager Workflows
1. **Log in** with manager credentials. You will be redirected to `/manager/dashboard` displaying team, criteria, and employee counts.
2. Navigate to **Criteria** (`/manager/criteria`):
   - Add evaluation criteria with weights (1 to 100) and descriptions.
   - Monitor the total active weight to ensure criteria sum to 100% for evaluation cycles.
   - Edit, deactivate, or delete criteria as needed. Criteria are snapshotted into evaluations at creation time.
3. Navigate to **Teams** (`/manager/teams`):
   - Create a team by entering a name, description, and selecting active employees from the member picker with assigned roles (e.g., `Tech Lead`, `Member`).
   - Click **Open** on any team to view `/manager/teams/view?id=N`.
   - Update team details, edit member roles, remove members, add new available employees, or delete the team.
   - Ownership is strictly enforced: managers cannot view or modify teams belonging to other managers.

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
  service/      — Service interfaces (UserService, TeamService, CriterionService, EvaluationService, GoalService, FeedbackService, ReportService, UserLookupService, UserSummary), ScoreCalculator, GoalProgressRule
  service/impl/ — Service implementations (including UserLookupServiceImpl)
  service/report/ — EmployeeReport, TeamReport, ManagerOverview, ReportAggregator, Trend
  servlet/      — BaseServlet, HomeServlet, LoginServlet, LogoutServlet, AdminDashboardServlet, AdminUsersServlet, ManagerDashboardServlet, ManagerCriteriaServlet, ManagerTeamsServlet, ManagerEvaluationsServlet, ManagerGoalsServlet, ManagerFeedbackServlet, EmployeeDashboardServlet, ErrorServlet, HealthServlet
  util/         — ConnectionFactory, TransactionManager, SchemaInitializer, Validator, CsrfTokens, CsrfSupport, Flash, FlashMessage, FormParams, DateFormatter, EvaluationFormParser

src/main/resources/
  db/schema.sql — Idempotent PostgreSQL DDL script

src/main/webapp/
  index.jsp
  assets/
    css/style.css
    js/evaluation-form.js
  WEB-INF/web.xml
  WEB-INF/tags/
    stageBadge.tag
    statusBadge.tag
  WEB-INF/tlds/
    format.tld
  WEB-INF/views/
    login.jsp
    error.jsp
    admin/
      users.jsp
    manager/
      criteria.jsp
      teams.jsp
      team-detail.jsp
      evaluations.jsp
      evaluation-form.jsp
      evaluation-detail.jsp
      goals.jsp
      feedback.jsp
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
  service/      — BaseServiceTest, ScoreCalculatorTest, GoalProgressRuleTest, AuthServiceTest, UserServiceTest, TeamServiceTest, CriterionServiceTest, EvaluationServiceTest, GoalServiceTest, FeedbackServiceTest, ReportServiceTest, UserLookupServiceTest
  service/report/ — ReportAggregatorTest
  util/         — ValidatorTest, TransactionRollbackTest, CsrfTokensTest, FlashTest, FormParamsTest, DateFormatterTest, EvaluationFormParserTest
  SmokeDataCleanupTest.java

db/             — Database documentation & data dictionary (README.md)
docs/           — College project documentation (including 04-user-guide.md)
```

## Project Status

- [x] **Phase 0** — Maven WAR skeleton, config, health check
- [x] **Phase 1** — Database schema, domain model (OOP), JDBC DAOs, transaction support
- [x] **Phase 2** — Services, validation, scoring, reports, concurrency
- [x] **Phase 3** — Authentication, authorization filters, CSRF protection, error handling, dashboard shells
- [x] **Phase 4A** — User admin, evaluation criteria, and team management with member roles
- [x] **Phase 4B** — Manager evaluations with Rocket Stage scoring, goals, and feedback workflows
- [ ] **Phase 4C** — Employee views, reporting dashboards, and visual charts

