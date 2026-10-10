# Requirement Analysis: Employee Performance Evaluation System (EPES)

## 1. Introduction

### 1.1 Purpose
This document records what the Employee Performance Evaluation System must do. It is the base for the design diagrams ([02-design-diagrams.md](02-design-diagrams.md)) and the solution design ([03-solution-design.md](03-solution-design.md)).

### 1.2 Problem statement
Organisations need one place where managers evaluate employee performance, set goals and give feedback, and where employees can see their own progress. The aim is to build a web platform that:

- lets managers evaluate employee performance, set goals and provide feedback,
- lets employees track their progress, view feedback and set personal goals,
- generates performance reports and analytics for both.

### 1.3 Scope
**In scope**
- Manager and Employee functions given in the college brief.
- A small Admin role that creates users and promotes people to Manager (the brief has only Manager and Employee, but someone must be able to create managers).
- Manager-defined evaluation criteria, a custom rating scale (Rocket Stages), team management where one employee can be in many teams.
- Reports on screen, with PDF and CSV export.

**Out of scope for the first submission**
- Google Sign-In (a temporary email login is used first, behind an interface so it can be swapped).
- Cloud deployment, email notifications, payroll or HR integration.

### 1.4 Definitions
| Term | Meaning |
|---|---|
| Criterion | A skill or behaviour a manager scores (for example "Teamwork"). Defined by the manager, with a weight. |
| Weight | Importance of a criterion, integer 1 to 100. |
| Evaluation | One review of one employee for one period (for example "Q3 2026"), made of one score per criterion. |
| Rocket Stage | Named performance level: Launchpad, Liftoff, Orbit, Cruise, Summit. |
| Goal | A target with a description, a target date, a status and progress percent. |
| DAO | Data Access Object: a class that does the SQL for one table or group of tables. |

## 2. Users and roles

| Role | Who | Main abilities |
|---|---|---|
| Manager | Team lead | Evaluate employees, set goals, give feedback, generate reports, manage teams, define criteria |
| Employee | Staff member | Track own performance, view feedback, set personal goals, view own reports |
| Admin | System owner | Create users, change roles, activate or deactivate accounts. First admin comes from the `ADMIN_EMAIL` environment variable. |

## 3. Functional requirements

### 3.1 Manager functions (from the college brief)

| ID | Function | Input | Output | Functionality |
|---|---|---|---|---|
| FR-M1 | Performance Evaluation | Evaluation criteria, employee details | Evaluation report | Evaluate employee performance on predefined criteria |
| FR-M2 | Goal Setting | Goal details (description, target date) | Confirmation message | Set goals for employees |
| FR-M3 | Feedback Provision | Feedback details (comments, rating) | Confirmation message | Give feedback to employees |
| FR-M4 | Performance Reports | Report request | Detailed performance report | Generate reports for analysis |
| FR-M5 | Team Management | Team details (members, roles) | Confirmation message | Manage teams and assign roles to members |
| FR-M6 | Criteria Management (added) | Name, description, weight | Confirmation message | Manager defines the criteria used in evaluations |

### 3.2 Employee functions (from the college brief)

| ID | Function | Input | Output | Functionality |
|---|---|---|---|---|
| FR-E1 | Performance Tracking | Performance data | Performance progress chart | Track own progress over time |
| FR-E2 | View Feedback | Request for feedback | List of feedback received | View feedback given by managers |
| FR-E3 | Set Personal Goals | Goal details (description, target date) | Confirmation message | Set and track personal goals |
| FR-E4 | Performance Reports | Report request | Detailed performance report | Report on own performance for self-assessment |

### 3.3 Admin functions (added)

| ID | Function | Input | Output | Functionality |
|---|---|---|---|---|
| FR-A1 | Create user | Email, full name, role | Confirmation | Register a person who may sign in |
| FR-A2 | Change role | User, new role | Confirmation | Promote or change a user role |
| FR-A3 | Activate or deactivate | User | Confirmation | Disable an account without deleting history |

### 3.4 System functions

| ID | Function | Description |
|---|---|---|
| FR-S1 | Authentication | Sign in with email. Only active registered users may enter. Session keeps the user logged in. |
| FR-S2 | Authorisation | Every page and every service call checks the role and ownership. |
| FR-S3 | Overdue goal check | A background thread marks goals that passed their target date as MISSED. |
| FR-S4 | Report export | Reports are exported to PDF and CSV by background worker threads. |
| FR-S5 | Schema and admin bootstrap | On startup the database tables are created if missing and the first admin is created from `ADMIN_EMAIL`. |

## 4. Dashboards

**Manager dashboard**
| Component | Content |
|---|---|
| Performance Evaluation | Table listing evaluations with details |
| Goal Setting | Table listing goals set for employees |
| Feedback Provision | Table listing feedback provided |
| Performance Reports | Graphical representation of performance data |
| Team Management | Panel for managing team members and roles |

**Employee dashboard**
| Component | Content |
|---|---|
| Performance Tracking | Chart showing performance progress over time |
| Feedback History | List of feedback received from managers |
| Personal Goals | Interface for setting and tracking personal goals |
| Performance Reports | Graphical representation of personal performance data |

**Admin dashboard**: user table, create user form, role change, activate or deactivate.

## 5. Business rules

| ID | Rule |
|---|---|
| BR-1 | Email is unique and stored in lower case. |
| BR-2 | Only an Admin can create users or change roles. An admin cannot demote or deactivate themselves. The last active admin can never be demoted or deactivated. |
| BR-3 | A team belongs to exactly one manager. An employee can belong to many teams. Only users with role Employee can be team members. |
| BR-4 | A manager can evaluate only active employees who are in a team that manager owns. |
| BR-5 | A criterion belongs to the manager who made it. Weight must be 1 to 100. Name is unique per manager. |
| BR-6 | An evaluation needs at least one criterion. Rating per criterion is 1 to 5. |
| BR-7 | One manager cannot evaluate the same employee twice for the same period label. |
| BR-8 | Criterion name and weight are copied (snapshot) into each score, so old evaluations never change if the criterion is edited later. |
| BR-9 | An evaluation and all its scores are saved together or not at all. |
| BR-10 | Goal target date must be today or later when created. Progress is 0 to 100. Progress 0 means NOT_STARTED, 1 to 99 means IN_PROGRESS, 100 means COMPLETED. |
| BR-11 | A manager-assigned goal can be changed only by the manager who set it (the employee may update its progress). A personal goal can be changed only by its employee. |
| BR-12 | Feedback rating is 1 to 5. Comments are required. |
| BR-13 | An employee sees only their own data. A manager sees only employees in their teams. An admin can read everything. |
| BR-14 | Deactivated users cannot sign in. Their history is kept. |

## 6. Rating model: Rocket Stages

Each criterion is rated 1 to 5. The weighted overall score (0 to 100) decides the stage.

```
overall_score = ( sum(rating_i * weight_i) / (5 * sum(weight_i)) ) * 100
```

Rounded to 2 decimals (half up).

| Rating | Stage | | Overall score | Stage |
|---|---|---|---|---|
| 1 | Launchpad | | below 40 | Launchpad |
| 2 | Liftoff | | 40 to below 60 | Liftoff |
| 3 | Orbit | | 60 to below 75 | Orbit |
| 4 | Cruise | | 75 to below 90 | Cruise |
| 5 | Summit | | 90 and above | Summit |

**Worked example.** Teamwork rating 4 (weight 50), Quality rating 3 (weight 30), Delivery rating 5 (weight 20).
(4x50 + 3x30 + 5x20) / (5 x 100) x 100 = (200 + 90 + 100) / 500 x 100 = **78.00, Cruise**.

## 7. Non-functional requirements

| ID | Area | Requirement |
|---|---|---|
| NFR-1 | Security | No passwords stored. Sessions expire after 30 minutes. All SQL uses prepared statements. Secrets come from environment variables, never from code or the repository. |
| NFR-2 | Integrity | Multi-step writes use database transactions with commit and rollback. Database constraints (keys, checks, unique) back up the application rules. |
| NFR-3 | Usability | Simple, clean pages with Bootstrap. Clear confirmation and error messages. Empty states shown when there is no data. |
| NFR-4 | Maintainability | Layered code (servlet, service, DAO). Interfaces for DAOs and services. Short Javadoc on classes. |
| NFR-5 | Reliability | Application errors map to clear messages. A failed background task never stops the scheduler. |
| NFR-6 | Portability | Runs on any machine with JDK 17 or newer, Maven and PostgreSQL. Settings through environment variables. |
| NFR-7 | Concurrency | Background work (deadline check, exports) runs on managed thread pools and shuts down cleanly. |

## 8. Constraints and technology

| Item | Choice | Reason |
|---|---|---|
| Language | Java (compiled for release 17) | College requires a Java web project |
| Web | Jakarta Servlet 6 and JSP on Tomcat 10.1 | Servlets and web integration are graded |
| Data access | Plain JDBC with DAO pattern | JDBC, CRUD and transaction handling are graded |
| Database | PostgreSQL | Strong constraints, free hosting available later |
| Build | Maven (WAR) | Standard Java build |
| UI | JSP, Bootstrap 5, Chart.js | Simple and polished |

## 9. Assumptions

1. Performance data is entered by the manager through evaluations. Employees do not enter scores about themselves.
2. The progress chart of an employee is drawn from the overall scores of their evaluations over time.
3. An employee can have evaluations from more than one manager if they are in teams of different managers.
4. Real data only: the system ships with no demo rows. Only the first admin is created at startup.

## 10. Traceability

| Requirement | Main code area | Tables |
|---|---|---|
| FR-M1, BR-4 to BR-9 | EvaluationService, ScoreCalculator, EvaluationDao | evaluations, evaluation_scores, criteria |
| FR-M2, FR-E3, BR-10, BR-11 | GoalService, GoalDao | goals |
| FR-M3, FR-E2, BR-12 | FeedbackService, FeedbackDao | feedback |
| FR-M4, FR-E1, FR-E4 | ReportService, ReportJobService | evaluations, goals, feedback |
| FR-M5, BR-3 | TeamService, TeamDao | teams, team_members |
| FR-M6, BR-5 | CriterionService, CriterionDao | criteria |
| FR-A1 to FR-A3, BR-1, BR-2, BR-14 | UserService, UserDao | users |
| FR-S1, FR-S2 | AuthService, authentication and role filters | users |
| FR-S3 | GoalDeadlineScheduler | goals |
| FR-S5 | SchemaInitializer, AppStartupListener | all |
