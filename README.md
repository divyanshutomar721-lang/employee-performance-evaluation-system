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
| Frontend   | JSP + Bootstrap 5 (CDN) + Chart.js (CDN — later)     |
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

Unit tests (pure math, validators, rules, schedulers) and service integration tests run via Maven Surefire:
```bash
mvn test
```
*Note*: Service integration tests use unique test data namespaces (`*@epes.test`, `T-*`) with FK-safe teardown ensuring zero test rows remain in the database.

## Project Structure

```
src/main/java/com/epes/
  concurrent/   — GoalDeadlineScheduler, ReportJobService, JobStatus
  config/       — DatabaseConfig, AppConstants, AppContext
  dao/          — DAO interfaces (UserDao, TeamDao, CriterionDao, EvaluationDao, GoalDao, FeedbackDao)
  dao/impl/     — Plain JDBC DAO implementations
  exception/    — ServiceException hierarchy (Validation, Auth, BusinessRule) & DataAccessException
  filter/       — Servlet filters
  listener/     — AppStartupListener (schema init, admin bootstrap, AppContext lifecycle)
  model/        — Domain objects (User, Admin, Manager, Employee, Team, Criterion, Evaluation, Goal, Feedback, RocketStage)
  service/      — Service interfaces, ScoreCalculator, GoalProgressRule
  service/impl/ — Service implementations
  service/report/ — EmployeeReport, TeamReport, ManagerOverview, ReportAggregator, Trend
  servlet/      — HTTP servlets (HealthServlet)
  util/         — ConnectionFactory, TransactionManager, SchemaInitializer, Validator

src/main/resources/
  db/schema.sql — Idempotent PostgreSQL DDL script

src/main/webapp/
  index.jsp
  WEB-INF/web.xml
  WEB-INF/views/
  assets/css/  assets/js/

src/test/java/com/epes/
  concurrent/   — GoalDeadlineSchedulerTest, ReportJobServiceTest
  dao/          — BaseDaoTest, UserDaoTest, TeamDaoTest, CriterionDaoTest, EvaluationDaoTest, GoalDaoTest, FeedbackDaoTest
  model/        — RocketStageTest
  service/      — BaseServiceTest, ScoreCalculatorTest, GoalProgressRuleTest, AuthServiceTest, UserServiceTest, TeamServiceTest, CriterionServiceTest, EvaluationServiceTest, GoalServiceTest, FeedbackServiceTest, ReportServiceTest
  service/report/ — ReportAggregatorTest
  util/         — ValidatorTest, TransactionRollbackTest

db/             — Database documentation & data dictionary (README.md)
docs/           — College project documentation
```

## Project Status

- [x] **Phase 0** — Maven WAR skeleton, config, health check
- [x] **Phase 1** — Database schema, domain model (OOP), JDBC DAOs, transaction support
- [x] **Phase 2** — Services, validation, scoring, reports, concurrency
- [ ] Phase 3 — Authentication, authorization filters, and servlets
- [ ] Phase 4 — JSP views, dashboards, Bootstrap 5 UI & charts
