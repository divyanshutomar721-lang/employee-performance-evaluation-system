# EPES Database Documentation & Data Dictionary

This document serves as the data dictionary and ER reference for the **Employee Performance Evaluation System (EPES)**.

---

## 1. Entity-Relationship Overview (Text Diagram)

```
       +---------------------------------------------+
       |                    USERS                    |
       |---------------------------------------------|
       | id (PK)                                     |
       | email (UQ), full_name, role, active, created|
       +---------------------------------------------+
         | 1          | 1          | 1          | 1
         |            |            |            |
         | manages    | manages    | member of  | assigned / author
         v            v            v            v
    +---------+  +----------+  +--------------+ +-------------------+
    |  TEAMS  |  | CRITERIA |  | TEAM_MEMBERS | | GOALS & FEEDBACK  |
    +---------+  +----------+  +--------------+ +-------------------+
         | 1          | 1
         | has        | evaluated by
         v            v
  +--------------------------------------------------+
  |                   EVALUATIONS                    |
  |--------------------------------------------------|
  | id (PK), employee_id (FK), manager_id (FK)       |
  | team_id (FK), period_label, overall_score, stage |
  +--------------------------------------------------+
         | 1
         | contains
         v
  +--------------------------------------------------+
  |                EVALUATION_SCORES                 |
  |--------------------------------------------------|
  | id (PK), evaluation_id (FK), criterion_id (FK)   |
  | criterion_name, weight, rating, comment          |
  +--------------------------------------------------+
```

---

## 2. Data Dictionary

### Table: `users`
Represents system actors (Admin, Manager, Employee).

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `email` | `VARCHAR(255)` | No | - | Unique, stored in lowercase |
| `full_name` | `VARCHAR(150)` | No | - | User display name |
| `role` | `VARCHAR(20)` | No | - | Check: `ADMIN`, `MANAGER`, `EMPLOYEE` |
| `active` | `BOOLEAN` | No | `TRUE` | Soft-deletion flag |
| `created_at` | `TIMESTAMP` | No | `now()` | Timestamp of registration |

### Table: `teams`
Departments or project teams managed by a manager.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `name` | `VARCHAR(150)` | No | - | Unique team name |
| `description`| `TEXT` | Yes | `NULL` | Team description/charter |
| `manager_id` | `BIGINT` | No | - | Foreign Key -> `users(id)` |
| `created_at` | `TIMESTAMP` | Yes | `now()` | Creation timestamp |

### Table: `team_members`
Associative junction table enabling many-to-many relationships between employees and teams.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `team_id` | `BIGINT` | No | - | FK -> `teams(id)` ON DELETE CASCADE |
| `employee_id`| `BIGINT` | No | - | FK -> `users(id)` |
| `role_in_team`| `VARCHAR(100)`| No | - | e.g. "Lead Developer", "Tester" |
| `joined_at` | `TIMESTAMP` | Yes | `now()` | Joining timestamp |
| **PK** | `(team_id, employee_id)` | | | Composite Primary Key |

### Table: `criteria`
Performance evaluation criteria defined by managers.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `manager_id` | `BIGINT` | No | - | FK -> `users(id)` |
| `name` | `VARCHAR(100)` | No | - | Criterion title (e.g., "Code Quality") |
| `description`| `TEXT` | Yes | `NULL` | Explanation of grading |
| `weight` | `INT` | No | - | Check: `1 <= weight <= 100` |
| `active` | `BOOLEAN` | Yes | `TRUE` | Soft active flag |
| **UQ** | `(manager_id, name)` | | | Unique per manager |

### Table: `evaluations`
Periodic review headers submitted by managers for employees.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `employee_id`| `BIGINT` | No | - | FK -> `users(id)` (Subject) |
| `manager_id` | `BIGINT` | No | - | FK -> `users(id)` (Reviewer) |
| `team_id` | `BIGINT` | Yes | `NULL` | FK -> `teams(id)` ON DELETE SET NULL |
| `period_label`| `VARCHAR(100)`| No | - | e.g., "Q3 2026", "Annual 2026" |
| `overall_score`| `NUMERIC(5,2)`| No | - | Check: `0 <= overall_score <= 100` |
| `overall_stage`| `VARCHAR(20)` | No | - | Check: `LAUNCHPAD`, `LIFTOFF`, `ORBIT`, `CRUISE`, `SUMMIT` |
| `summary_comment`| `TEXT` | Yes | `NULL` | General summary remarks |
| `created_at` | `TIMESTAMP` | Yes | `now()` | Submission timestamp |

### Table: `evaluation_scores`
Criterion-level rating snapshots linked to an evaluation.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `evaluation_id`| `BIGINT` | No | - | FK -> `evaluations(id)` ON DELETE CASCADE |
| `criterion_id` | `BIGINT` | Yes | `NULL` | FK -> `criteria(id)` ON DELETE SET NULL |
| `criterion_name`| `VARCHAR(100)`| No| - | Snapshot copy of criterion name |
| `weight` | `INT` | No | - | Snapshot copy of weight |
| `rating` | `SMALLINT` | No | - | Check: `1 <= rating <= 5` |
| `comment` | `TEXT` | Yes | `NULL` | Specific criterion feedback |

### Table: `goals`
Personal or manager-assigned employee goals with completion tracking.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `employee_id`| `BIGINT` | No | - | FK -> `users(id)` |
| `set_by` | `BIGINT` | No | - | FK -> `users(id)` |
| `goal_type` | `VARCHAR(20)` | No | - | Check: `MANAGER_ASSIGNED`, `PERSONAL` |
| `description`| `TEXT` | No | - | Goal statement |
| `target_date`| `DATE` | No | - | Target completion date |
| `status` | `VARCHAR(20)` | No | `'NOT_STARTED'` | Check: `NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `MISSED` |
| `progress_percent`| `SMALLINT` | No | `0` | Check: `0 <= progress_percent <= 100` |
| `created_at` | `TIMESTAMP` | Yes | `now()` | Creation timestamp |
| `updated_at` | `TIMESTAMP` | Yes | `now()` | Last progress update timestamp |

### Table: `feedback`
Continuous 360/manager feedback and ratings outside formal evaluation cycles.

| Column | Type | Nullable | Default | Constraints / Description |
|---|---|---|---|---|
| `id` | `BIGSERIAL` | No | Auto | Primary Key |
| `employee_id`| `BIGINT` | No | - | FK -> `users(id)` (Recipient) |
| `manager_id` | `BIGINT` | No | - | FK -> `users(id)` (Sender) |
| `evaluation_id`| `BIGINT` | Yes | `NULL` | FK -> `evaluations(id)` ON DELETE SET NULL |
| `comments` | `TEXT` | No | - | Feedback content |
| `rating` | `SMALLINT` | No | - | Check: `1 <= rating <= 5` |
| `created_at` | `TIMESTAMP` | Yes | `now()` | Submission timestamp |
