# EPES Testing Summary

This document summarizes the automated test suite and live integration smoke scenarios for the Employee Performance Evaluation System (EPES), confirming test coverage, execution metrics, and database isolation guarantees.

---

## 1. Test Suite Coverage by Layer

The test suite contains 112 automated unit and integration tests across 33 test classes, executing in under 25 seconds via Maven Surefire and JUnit 5 Jupiter.

| Layer | Test Class | Coverage Description |
|---|---|---|
| **Model** | `com.epes.model.RocketStageTest` | Verifies score threshold boundaries for the 5 Rocket Stages (Launchpad, Liftoff, Orbit, Cruise, Summit). |
| **Util** | `com.epes.util.ValidatorTest` | Validates email syntax, required strings, numeric ranges, and field boundaries. |
| **Util** | `com.epes.util.DateFormatterTest` | Verifies date/time formatting to "dd MMM yyyy", "dd MMM yyyy HH:mm", and two-decimal number formatting ("88.00"). |
| **Util** | `com.epes.util.FormParamsTest` | Validates safe extraction and type parsing of integer, long, double, and string form parameters. |
| **Util** | `com.epes.util.EvaluationFormParserTest` | Verifies extraction and dynamic snapshot mapping of criteria ratings and comments from HTTP POST parameters. |
| **Util** | `com.epes.util.CsrfTokensTest` | Tests cryptographically secure token generation and constant-time validation (`MessageDigest.isEqual`). |
| **Util** | `com.epes.util.FlashTest` | Tests session-based flash messaging lifecycle across redirects (info, success, warning, danger). |
| **Util** | `com.epes.util.ChartJsonTest` | Tests pure JSON generation for line, bar, and doughnut charts, asserting safe escaping of quotes, backslashes, and HTML tags (`\u003c`, `\u003e`, `\u0026`). |
| **Util** | `com.epes.util.TransactionRollbackTest` | Verifies atomic rollback on checked and runtime exceptions within `TransactionManager.executeInTransaction`. |
| **Filter** | `com.epes.filter.AccessPolicyTest` | Verifies path-based access control rules and role mappings across `/admin/*`, `/manager/*`, `/employee/*`, and public endpoints. |
| **Concurrent** | `com.epes.concurrent.GoalDeadlineSchedulerTest` | Verifies periodic daemon thread execution detecting overdue incomplete goals and transitioning them to `MISSED`. |
| **Concurrent** | `com.epes.concurrent.ReportJobServiceTest` | Verifies multi-threaded worker pool execution, job submission, status transitions, and LRU history eviction. |
| **DAO** | `com.epes.dao.BaseDaoTest` | Base integration harness managing test connection pooling and database initialization. |
| **DAO** | `com.epes.dao.UserDaoTest` | CRUD operations for users, active state toggling, and role updates via plain JDBC. |
| **DAO** | `com.epes.dao.TeamDaoTest` | Team creation, member additions, role assignments, and cascade deletion. |
| **DAO** | `com.epes.dao.CriterionDaoTest` | CRUD operations for evaluation criteria, manager scoping, and active weight calculations. |
| **DAO** | `com.epes.dao.EvaluationDaoTest` | Persisting evaluation reviews with snapshot scores, period uniqueness, and employee score lookups. |
| **DAO** | `com.epes.dao.GoalDaoTest` | Personal and manager-assigned goal persistence, progress tracking, and target date filtering. |
| **DAO** | `com.epes.dao.FeedbackDaoTest` | Continuous feedback insertion, employee lookup, rating storage, and deletion. |
| **Service** | `com.epes.service.BaseServiceTest` | Integration test base providing isolated test context and cleanup helpers. |
| **Service** | `com.epes.service.ScoreCalculatorTest` | Validates weighted evaluation score formula and Rocket Stage mapping. |
| **Service** | `com.epes.service.GoalProgressRuleTest` | Tests automatic progress-to-status mapping (0 = NOT_STARTED, 1..99 = IN_PROGRESS, 100 = COMPLETED). |
| **Service** | `com.epes.service.AuthServiceTest` | Validates authentication, password-less development authentication, and user credential validation. |
| **Service** | `com.epes.service.UserServiceTest` | Enforces user administration business rules (unique emails, last admin demotion prevention, self-deactivation prevention). |
| **Service** | `com.epes.service.UserLookupServiceTest` | Tests optimized user summary lookups and active employee roster retrieval. |
| **Service** | `com.epes.service.TeamServiceTest` | Enforces manager team ownership, member assignments, and atomic multi-member team creation. |
| **Service** | `com.epes.service.CriterionServiceTest` | Enforces criterion weights (1..100), active weight limits, and manager isolation. |
| **Service** | `com.epes.service.EvaluationServiceTest` | Validates evaluation creation with snapshotting, team membership enforcement, and period duplication defense. |
| **Service** | `com.epes.service.GoalServiceTest` | Tests assigned and personal goal creation, progress updates, past target date refusal, and role permissions. |
| **Service** | `com.epes.service.FeedbackServiceTest` | Tests feedback submission (1..5 rating), evaluation linking, and supervisor access boundaries. |
| **Service** | `com.epes.service.ReportServiceTest` | Tests aggregate report generation for employee reports, team reports, manager overview, and score lookups. |
| **Service** | `com.epes.service.report.ReportAggregatorTest` | Tests pure aggregations: trajectory trend detection, stage distribution histogram, and criterion average calculation. |
| **Smoke** | `com.epes.SmokeDataCleanupTest` | Idempotent cleanup test asserting zero test rows remain in the live database. |

---

## 2. Test Execution Metrics (Phase 4C)

```text
Results:
Tests run: 112, Failures: 0, Errors: 0, Skipped: 0
Build: SUCCESS
```

---

## 3. Database Isolation Policy: "Tests Leave No Data in the Database"

EPES enforces a strict database isolation principle:
- **Test User Isolation**: All integration and smoke test entities use unique email identifiers ending with `@epes.test`.
- **Automatic Rollback / Cleanup Harness**: Unit tests interacting with the database execute within transaction boundaries or utilize `@AfterEach` teardown hooks to purge created entities in reverse dependency order.
- **`SmokeDataCleanupTest` Enforcement**: Following any live smoke test run or integration run, `SmokeDataCleanupTest` purges any test data across all tables (`feedback`, `evaluation_scores`, `evaluations`, `goals`, `team_members`, `teams`, `criteria`, `users`) and asserts that exactly **zero** rows matching `@epes.test` remain in the database.
- **No Manual Database Modification**: No manual `psql` or direct DB manipulation is performed.

---

## 4. Live Smoke Test Scenarios (Across Phases)

Live end-to-end smoke testing executes against a running Tomcat container (via Cargo on port 8080) using PowerShell session-based web requests and CSRF extraction:

1. **Authentication & Session Lifecycle**:
   - GET `/login` checks CSRF token extraction and session renewal upon authentication.
   - Session fixation defense verified (old session invalidated, new session issued).
   - POST `/logout` terminates session and clears cookies.
2. **CSRF Protection**:
   - Rejects POST requests with missing or tampered `_csrf` tokens with HTTP 403 Forbidden.
3. **Role-Based Authorization & Boundaries**:
   - `ADMIN` restricted to `/admin/*`.
   - `MANAGER` restricted to `/manager/*` and supervised team entities.
   - `EMPLOYEE` restricted to `/employee/*` and personal data records.
   - Cross-user permission boundaries tested: Employee accessing another employee's evaluation view returns HTTP 403; Manager requesting an unmanaged team or employee report returns HTTP 403.
4. **Evaluations Workflow**:
   - Manager creates criteria and team with assigned employees.
   - Manager submits multiple evaluation reviews across review periods (`T-Q1`, `T-Q2`).
   - Weighted score calculation and Rocket Stage badges (`Liftoff`, `Cruise`) verified.
5. **Goals Management & Ownership**:
   - Manager assigns a goal to an employee.
   - Employee views assigned goal with edit/delete controls disabled.
   - Employee creates personal goal, updates progress to 100%, and verifies automatic `COMPLETED` status.
   - Employee attempting to delete manager-assigned goal is strictly refused.
6. **Continuous Feedback**:
   - Manager submits continuous feedback (rating 4/5) with comments.
   - Employee views feedback history with Rocket Stage tier labels and manager attribution.
7. **Reporting & Visual Analytics**:
   - Employee views `/employee/reports`: verifies 6 KPI cards, criterion averages (`3.00`, `4.00`), and 3 safe Chart.js canvases (`line`, `doughnut`, `bar`).
   - Manager views `/manager/reports`: verifies managed team overview, team averages (`68.00`), stage distribution bar chart, and ranked member table (un-evaluated employees sorted last).
8. **HTML Escaping & XSS Defense**:
   - Criterion created with HTML name `<b>x</b>`.
   - Verified that all pages and `<canvas data-chart="...">` attributes display safely escaped text (`&lt;b&gt;x&lt;/b&gt;`) with zero raw script or markup execution.
9. **Self-Contained Local Assets**:
   - Verified that Bootstrap CSS, Bootstrap Bundle JS, and Chart.js UMD are served locally from `/assets/vendor/` with HTTP 200 and >100 KB payloads without external CDN dependencies.
