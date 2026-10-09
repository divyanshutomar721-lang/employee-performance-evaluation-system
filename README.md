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

2. **Set the environment variables** for your PostgreSQL user:
   ```bash
   # Linux / macOS
   export DB_PASSWORD=your_password
   export ADMIN_EMAIL=admin@epes.local   # optional bootstrap admin

   # Windows (PowerShell)
   $env:DB_PASSWORD = "your_password"
   $env:ADMIN_EMAIL = "admin@epes.local" # optional bootstrap admin
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

## Database

The PostgreSQL schema is located at `src/main/resources/db/schema.sql` and is auto-initialized on application startup via `SchemaInitializer`.
The schema contains 8 core tables:
- `users`: Core account data, roles (`ADMIN`, `MANAGER`, `EMPLOYEE`), soft active flag.
- `teams`: Organizational units assigned to a manager.
- `team_members`: Association table mapping employees to multiple teams with custom roles.
- `criteria`: Manager-defined evaluation criteria and relative weights.
- `evaluations`: Formal quarterly/periodic reviews with aggregate scores and `RocketStage` bands.
- `evaluation_scores`: Immutable snapshot ratings and commentary per criterion.
- `goals`: Employee goals (personal and manager-assigned) with lifecycle status and progress.
- `feedback`: Continuous 360 commentary and ratings.

Full data dictionary and text ER diagrams are documented in [db/README.md](db/README.md).

## Running Tests

Unit tests (like `RocketStageTest`) and integration tests run via Maven Surefire:
```bash
# Ensure DB_PASSWORD is set in the environment for DB integration tests
mvn test
```
*Note*: Integration tests use transactional rollback (`autoCommit=false` + `rollback()` in teardown) ensuring no rows are committed to the database.

## Project Structure

```
src/main/java/com/epes/
  config/       — DatabaseConfig, AppConstants
  dao/          — DAO interfaces (UserDao, TeamDao, CriterionDao, EvaluationDao, GoalDao, FeedbackDao)
  dao/impl/     — Plain JDBC DAO implementations
  exception/    — ConfigurationException, DataAccessException, EntityNotFoundException, DuplicateEntityException
  filter/       — Servlet filters
  listener/     — AppStartupListener (schema init & admin bootstrap)
  model/        — Domain objects (User, Admin, Manager, Employee, Team, Criterion, Evaluation, Goal, Feedback, RocketStage)
  service/      — Business logic services
  servlet/      — HTTP servlets (HealthServlet)
  util/         — ConnectionFactory, TransactionManager, SchemaInitializer

src/main/resources/
  db/schema.sql — Idempotent PostgreSQL DDL script

src/main/webapp/
  index.jsp
  WEB-INF/web.xml
  WEB-INF/views/
  assets/css/  assets/js/

src/test/java/com/epes/
  dao/          — BaseDaoTest, UserDaoTest, TeamDaoTest, CriterionDaoTest, EvaluationDaoTest, GoalDaoTest, FeedbackDaoTest
  model/        — RocketStageTest
  util/         — TransactionRollbackTest

db/             — Database documentation & data dictionary (README.md)
docs/           — College project documentation
```

## Project Status

- [x] **Phase 0** — Maven WAR skeleton, config, health check
- [x] **Phase 1** — Database schema, domain model (OOP), JDBC DAOs, transaction support
- [ ] Phase 2 — Authentication & authorisation
- [ ] Phase 3 — Core features (evaluations, goals, feedback)
- [ ] Phase 4 — Reports & dashboards
