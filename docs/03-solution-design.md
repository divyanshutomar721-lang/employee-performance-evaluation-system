# Solution Design and Logical Plan: EPES

This document explains how the requirements in [01-requirement-analysis.md](01-requirement-analysis.md) are turned into code, and why. Diagrams are in [02-design-diagrams.md](02-design-diagrams.md).

## 1. Approach

The system is a layered Java web application with no framework. Plain Servlets, JSP and JDBC are used on purpose, so every core Java and web concept is visible in the code.

| Layer | Responsibility | Never does |
|---|---|---|
| Web (filters, servlets, JSP) | Read request, call a service, put the result on the request, choose the view. Manage the session. | Business rules, SQL |
| Service | Validation, authorisation, business rules, scoring, reports, transactions | HTTP objects, raw SQL |
| DAO | SQL for one group of tables with PreparedStatement. Map rows to objects. | Business rules, permission checks |
| Util and config | Connection creation, transactions, schema setup, settings from environment variables | Business logic |

## 2. Package structure

```
com.epes
 +-- config        DatabaseConfig, AppConstants, AppContext
 +-- model         User (abstract), Admin, Manager, Employee, UserFactory,
 |                 Team, TeamMember, Criterion, Evaluation, EvaluationScore,
 |                 Goal, Feedback, enums Role, RocketStage, GoalStatus, GoalType
 +-- dao           DAO interfaces
 +-- dao.impl      JDBC implementations
 +-- service       Service interfaces, ScoreCalculator
 +-- service.impl  Service implementations
 +-- service.report  EmployeeReport, TeamReport, ManagerOverview, ReportAggregator
 +-- concurrent    GoalDeadlineScheduler, ReportJobService
 +-- exception     Custom exceptions
 +-- listener      AppStartupListener
 +-- filter        Authentication and role filters
 +-- servlet       Servlets
 +-- util          ConnectionFactory, TransactionManager, TransactionalWork,
                   SchemaInitializer, Validator
```

## 3. Design patterns used

| Pattern | Where | Why |
|---|---|---|
| DAO | com.epes.dao and dao.impl | Keeps SQL out of the rest of the code. A DAO can be swapped or tested alone. |
| Factory | UserFactory | One place that builds the right subclass (Admin, Manager, Employee) from a Role. |
| Dependency injection by constructor | AppContext builds all DAOs and services once | Easy to test, no framework needed. |
| Strategy / interface | AuthService with a temporary email implementation | Google Sign-In can replace it without changing servlets. |
| Template of work | TransactionalWork and TransactionManager | One tested place for commit, rollback and close. |
| Front controller style filters | Authentication and role filters | Security checks in one place instead of in every servlet. |

## 4. Core Java concepts and where they appear

| Concept | Where used |
|---|---|
| OOP: abstraction, inheritance, polymorphism | Abstract `User` with `Admin`, `Manager`, `Employee`, each implementing `getDashboardPath()`. DAO and service interfaces with implementations. |
| OOP: encapsulation | Private fields with getters and setters in all model classes. Report classes with final fields. |
| Enums with behaviour | `RocketStage.fromScore`, `Role`, `GoalStatus`, `GoalType` |
| Collections | `List` for results, `Map` for criterion inputs and role-by-employee lookups, `EnumMap` for stage and goal counts, `Comparator` and streams for sorting and averages, `ConcurrentHashMap` for report jobs |
| Exception handling | Checked `DataAccessException` for database failures. Unchecked `ServiceException` family for rule violations. Custom exceptions carry clear messages. SQLState 23505 is mapped to `DuplicateEntityException`. try-with-resources everywhere. |
| Threads | `ScheduledExecutorService` for the goal deadline check. `ExecutorService` for report export. Daemon threads with names, `AtomicLong`, `volatile`, clean shutdown. |

## 5. Database design decisions

| Decision | Reason |
|---|---|
| Surrogate keys (BIGSERIAL) on every table except the join table | Simple, stable references |
| Composite key on `team_members` (team_id, employee_id) | One row per employee per team, many teams per employee allowed |
| `ON DELETE CASCADE` for team members and evaluation scores | Children have no meaning without the parent |
| `ON DELETE SET NULL` for optional links (evaluation team, score criterion, feedback evaluation) | History survives when a team or criterion is removed |
| Snapshot columns `criterion_name` and `weight` in `evaluation_scores` | Old evaluations never change when a criterion is edited (BR-8) |
| CHECK constraints (role, rating 1 to 5, weight 1 to 100, progress 0 to 100, score 0 to 100) | The database rejects bad data even if application code has a bug |
| UNIQUE on email, team name, (manager, criterion name) | Prevents duplicates at the lowest level |
| Soft delete for users (`active` flag) | Keeps history intact (BR-14) |
| Indexes on foreign keys and on (employee_id, created_at) for evaluations | Fast lists and charts |
| Third normal form | No repeated facts except the deliberate score snapshot |

Full table list: [../db/README.md](../db/README.md).

## 6. Transaction design

JDBC transactions are handled in one class, `TransactionManager.executeInTransaction(work)`:

1. get a connection, set auto-commit off,
2. run the work,
3. commit when it finishes,
4. roll back on any exception and log it,
5. restore auto-commit and close the connection.

Every DAO write method has a second form that takes a `Connection`, so several DAOs can join one transaction.

| Operation | What must succeed together |
|---|---|
| Create evaluation | evaluation row and all its score rows |
| Create team | team row and all member rows |
| Schema initialisation at startup | the whole schema script |
| Mark overdue goals | all status updates in one run |

A test proves the rollback: it inserts an evaluation inside a transaction, throws an error, and checks that nothing was stored.

## 7. Error handling and validation

- `Validator` collects all errors in one pass, then throws one `ValidationException` with the full list, so the user fixes everything at once.
- Services throw the exception that matches the problem (see the exception diagram). A single error-handling servlet and JSP page turn them into friendly messages. Technical details go to the log, never to the user page.
- The health page never reveals the database URL or credentials.

## 8. Security design

| Topic | Design |
|---|---|
| Authentication | Email login now (temporary, can be switched off by an environment variable). Google Sign-In later behind the same `AuthService` interface. |
| Session | `HttpSession` holds the logged-in user. Timeout 30 minutes. Session id is renewed on login. Logout invalidates the session. |
| Authorisation | Role filter on URL groups (`/admin/*`, `/manager/*`, `/employee/*`). Services check ownership again, so a manager cannot read another manager's team even by guessing a URL. |
| SQL injection | PreparedStatement with parameters only |
| Secrets | `DB_PASSWORD` and other settings come from environment variables. The repository holds only `.env.example`. |
| Output encoding | JSTL `c:out` for all user text in JSP |
| Forms | Create, update and delete actions use POST only |

## 9. Request life cycle

1. Browser sends a request.
2. Authentication filter checks the session. No user: redirect to the login page.
3. Role filter checks the URL group against the user role.
4. Servlet reads and converts parameters, gets the service from `AppContext`, and calls it with the logged-in user.
5. Service validates, checks ownership, runs the rule, calls DAOs (inside a transaction when it writes more than one table).
6. Servlet stores the result on the request and forwards to a JSP. After a successful POST it redirects instead (post, redirect, get) so a refresh does not repeat the action.

## 10. Concurrency design

| Component | Design |
|---|---|
| GoalDeadlineScheduler | Single daemon thread named `epes-goal-deadline`. Starts after 10 seconds, then repeats every `GOAL_CHECK_INTERVAL_MINUTES` (default 60). Catches and logs all exceptions inside the task so the schedule never dies. Counters use `AtomicLong` and `volatile`. `stop()` shuts down, waits up to 5 seconds, then forces. |
| ReportJobService | Pool of 2 daemon threads named `epes-report-N`. `submit` returns a job id, status is PENDING, RUNNING, DONE or FAILED. Jobs stored in a `ConcurrentHashMap` capped at 100 entries. Used for PDF and CSV export. |
| Shutdown | `AppStartupListener.contextDestroyed` stops both, so Tomcat shuts down without leftover threads. |

## 11. Logical plan and phases

| Phase | Content | Result |
|---|---|---|
| 0 | Maven WAR skeleton, config from environment, connection factory, health page | App runs, database reachable |
| 1 | Schema, domain model, DAOs, transaction support, tests | Data layer proven by automated tests |
| 2 | Services, validation, scoring, reports data, threads | Business rules enforced |
| 3 | Servlets, session login, role filters, error handling | Secure request handling |
| 4 | Manager, employee and admin pages with charts | Working demo |
| 5 | Export, UI polish, Google Sign-In, deployment | Final version |

## 12. Review 1 rubric mapping

| Deliverable (marks) | Evidence in the repository |
|---|---|
| Problem Understanding and Solution Design (8) | [01-requirement-analysis.md](01-requirement-analysis.md), [02-design-diagrams.md](02-design-diagrams.md), this document |
| Core Java Concepts (10) | Section 4 above. Code in `model`, `service`, `concurrent`, `exception`. Unit tests for scoring, validation, reports and threads. |
| Database Integration, JDBC (8) | `src/main/resources/db/schema.sql`, `db/README.md`, `dao` and `dao.impl`, `TransactionManager`, DAO integration tests and the rollback test |
| Servlets and Web Integration (7) | `servlet`, `filter`, `listener`, JSP views, session handling (Phase 3 and 4) |
