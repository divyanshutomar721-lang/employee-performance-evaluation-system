# Design Diagrams: EPES

All diagrams are written in Mermaid, so GitHub draws them directly in the browser. No image files are needed.

Contents: 1. Use case, 2. Architecture, 3. ER diagram, 4. Class diagram (domain), 5. Class diagram (layers), 6. Sequence diagrams, 7. Goal status state diagram, 8. Evaluation flow.

---

## 1. Use case diagram

```mermaid
flowchart LR
    A((Admin))
    M((Manager))
    E((Employee))

    subgraph SYS["Employee Performance Evaluation System"]
        UC1(["Sign in and out"])
        UC2(["Create user"])
        UC3(["Change role"])
        UC4(["Activate or deactivate user"])
        UC5(["Manage criteria"])
        UC6(["Manage teams and roles"])
        UC7(["Evaluate employee"])
        UC8(["Set goal for employee"])
        UC9(["Give feedback"])
        UC10(["Generate team report"])
        UC11(["Track own performance"])
        UC12(["View feedback"])
        UC13(["Set personal goal"])
        UC14(["Generate own report"])
        UC15(["Export report PDF or CSV"])
    end

    A --> UC1
    A --> UC2
    A --> UC3
    A --> UC4

    M --> UC1
    M --> UC5
    M --> UC6
    M --> UC7
    M --> UC8
    M --> UC9
    M --> UC10

    E --> UC1
    E --> UC11
    E --> UC12
    E --> UC13
    E --> UC14

    UC10 -.->|include| UC15
    UC14 -.->|include| UC15
```

---

## 2. Architecture (layers)

```mermaid
flowchart TB
    B["Browser"]
    subgraph WEB["Web layer (Tomcat 10.1)"]
        F["Filters: authentication and role check"]
        S["Servlets: request and response, session"]
        J["JSP views with Bootstrap and Chart.js"]
    end
    subgraph BIZ["Service layer"]
        SV["Services: validation, authorisation, business rules"]
        SC["ScoreCalculator and ReportAggregator"]
        TX["TransactionManager"]
    end
    subgraph DATA["Data layer"]
        D["DAO interfaces and JDBC implementations"]
        CF["ConnectionFactory and DatabaseConfig"]
    end
    subgraph BG["Background threads"]
        GS["GoalDeadlineScheduler"]
        RJ["ReportJobService"]
    end
    DB[("PostgreSQL")]

    B --> F --> S --> SV
    S --> J --> B
    SV --> SC
    SV --> TX --> D
    SV --> D
    D --> CF --> DB
    GS --> SV
    RJ --> SV
```

---

## 3. ER diagram

```mermaid
erDiagram
    USERS ||--o{ TEAMS : manages
    USERS ||--o{ TEAM_MEMBERS : "belongs as employee"
    TEAMS ||--o{ TEAM_MEMBERS : has
    USERS ||--o{ CRITERIA : defines
    USERS ||--o{ EVALUATIONS : "is evaluated"
    USERS ||--o{ EVALUATIONS : writes
    TEAMS |o--o{ EVALUATIONS : "context of"
    EVALUATIONS ||--|{ EVALUATION_SCORES : contains
    CRITERIA |o--o{ EVALUATION_SCORES : "snapshot of"
    USERS ||--o{ GOALS : "owns"
    USERS ||--o{ GOALS : "sets"
    USERS ||--o{ FEEDBACK : receives
    USERS ||--o{ FEEDBACK : gives
    EVALUATIONS |o--o{ FEEDBACK : "linked to"

    USERS {
        bigint id PK
        varchar email UK
        varchar full_name
        varchar role
        boolean active
        timestamp created_at
    }
    TEAMS {
        bigint id PK
        varchar name UK
        text description
        bigint manager_id FK
        timestamp created_at
    }
    TEAM_MEMBERS {
        bigint team_id PK
        bigint employee_id PK
        varchar role_in_team
        timestamp joined_at
    }
    CRITERIA {
        bigint id PK
        bigint manager_id FK
        varchar name
        text description
        int weight
        boolean active
    }
    EVALUATIONS {
        bigint id PK
        bigint employee_id FK
        bigint manager_id FK
        bigint team_id FK
        varchar period_label
        numeric overall_score
        varchar overall_stage
        text summary_comment
        timestamp created_at
    }
    EVALUATION_SCORES {
        bigint id PK
        bigint evaluation_id FK
        bigint criterion_id FK
        varchar criterion_name
        int weight
        smallint rating
        text comment
    }
    GOALS {
        bigint id PK
        bigint employee_id FK
        bigint set_by FK
        varchar goal_type
        text description
        date target_date
        varchar status
        smallint progress_percent
        timestamp created_at
        timestamp updated_at
    }
    FEEDBACK {
        bigint id PK
        bigint employee_id FK
        bigint manager_id FK
        bigint evaluation_id FK
        text comments
        smallint rating
        timestamp created_at
    }
```

The full data dictionary with every constraint is in [../db/README.md](../db/README.md).

---

## 4. Class diagram: domain model

```mermaid
classDiagram
    class User {
        <<abstract>>
        -Long id
        -String email
        -String fullName
        -Role role
        -boolean active
        -LocalDateTime createdAt
        +getDashboardPath() String
    }
    class Admin {
        +getDashboardPath() String
    }
    class Manager {
        +getDashboardPath() String
    }
    class Employee {
        +getDashboardPath() String
    }
    class UserFactory {
        +create(Role role, ...) User
    }
    class Team {
        -Long id
        -String name
        -String description
        -Long managerId
        -List~TeamMember~ members
    }
    class TeamMember {
        -Long teamId
        -Long employeeId
        -String roleInTeam
    }
    class Criterion {
        -Long id
        -Long managerId
        -String name
        -int weight
        -boolean active
    }
    class Evaluation {
        -Long id
        -Long employeeId
        -Long managerId
        -Long teamId
        -String periodLabel
        -double overallScore
        -RocketStage overallStage
        -List~EvaluationScore~ scores
    }
    class EvaluationScore {
        -Long criterionId
        -String criterionName
        -int weight
        -int rating
        -String comment
    }
    class Goal {
        -Long id
        -Long employeeId
        -Long setBy
        -GoalType goalType
        -String description
        -LocalDate targetDate
        -GoalStatus status
        -int progressPercent
    }
    class Feedback {
        -Long id
        -Long employeeId
        -Long managerId
        -Long evaluationId
        -String comments
        -int rating
    }
    class Role {
        <<enumeration>>
        ADMIN
        MANAGER
        EMPLOYEE
    }
    class RocketStage {
        <<enumeration>>
        LAUNCHPAD
        LIFTOFF
        ORBIT
        CRUISE
        SUMMIT
        +fromScore(double score) RocketStage
    }
    class GoalStatus {
        <<enumeration>>
        NOT_STARTED
        IN_PROGRESS
        COMPLETED
        MISSED
    }
    class GoalType {
        <<enumeration>>
        MANAGER_ASSIGNED
        PERSONAL
    }

    User <|-- Admin
    User <|-- Manager
    User <|-- Employee
    UserFactory ..> User : creates
    User --> Role
    Team "1" o-- "*" TeamMember
    Evaluation "1" *-- "1..*" EvaluationScore
    Evaluation --> RocketStage
    Goal --> GoalStatus
    Goal --> GoalType
    Manager "1" --> "*" Team : manages
    Manager "1" --> "*" Criterion : defines
    Manager "1" --> "*" Evaluation : writes
    Employee "1" --> "*" Evaluation : receives
    Employee "1" --> "*" Goal : owns
    Employee "1" --> "*" Feedback : receives
```

---

## 5. Class diagram: layers and DAO pattern

Shown for evaluations. The other entities follow the same pattern.

```mermaid
classDiagram
    class EvaluationServlet {
        +doGet(request, response)
        +doPost(request, response)
    }
    class EvaluationService {
        <<interface>>
        +createEvaluation(actor, employeeId, teamId, period, comment, inputs) Evaluation
        +getEvaluation(actor, id) Evaluation
        +listForEmployee(actor, employeeId) List
    }
    class EvaluationServiceImpl
    class ScoreCalculator {
        +computeOverallScore(List scores) double
        +stageFor(double score) RocketStage
    }
    class EvaluationDao {
        <<interface>>
        +saveWithScores(conn, evaluation, scores) Evaluation
        +findById(id) Optional
        +findByEmployee(id) List
    }
    class EvaluationDaoImpl
    class TransactionManager {
        +executeInTransaction(work) T
    }
    class ConnectionFactory {
        +getConnection() Connection
    }

    EvaluationServlet --> EvaluationService
    EvaluationService <|.. EvaluationServiceImpl
    EvaluationServiceImpl --> ScoreCalculator
    EvaluationServiceImpl --> EvaluationDao
    EvaluationServiceImpl --> TransactionManager
    EvaluationDao <|.. EvaluationDaoImpl
    EvaluationDaoImpl --> ConnectionFactory
    TransactionManager --> ConnectionFactory
```

Exception hierarchy:

```mermaid
classDiagram
    class Exception
    class RuntimeException
    class DataAccessException {
        checked
    }
    class ConfigurationException {
        unchecked
    }
    class ServiceException {
        <<abstract>>
        unchecked
    }
    class EntityNotFoundException
    class DuplicateEntityException
    class ValidationException {
        +getErrors() List
    }
    class AuthenticationException
    class AuthorizationException
    class BusinessRuleException

    Exception <|-- DataAccessException
    DataAccessException <|-- EntityNotFoundException
    DataAccessException <|-- DuplicateEntityException
    Exception <|-- RuntimeException
    RuntimeException <|-- ConfigurationException
    RuntimeException <|-- ServiceException
    ServiceException <|-- ValidationException
    ServiceException <|-- AuthenticationException
    ServiceException <|-- AuthorizationException
    ServiceException <|-- BusinessRuleException
```

---

## 6. Sequence diagrams

### 6.1 Sign in (temporary email login)

```mermaid
sequenceDiagram
    actor U as User
    participant LS as LoginServlet
    participant AS as AuthService
    participant UD as UserDao
    participant DB as PostgreSQL
    participant SE as HttpSession

    U->>LS: POST /login (email)
    LS->>AS: authenticate(email)
    AS->>UD: findByEmail(email)
    UD->>DB: SELECT ... WHERE lower(email) = ?
    DB-->>UD: row or nothing
    UD-->>AS: Optional of User
    alt user found and active
        AS-->>LS: User
        LS->>SE: create session, store user
        LS-->>U: redirect to role dashboard
    else not found or inactive
        AS-->>LS: AuthenticationException
        LS-->>U: login page with error message
    end
```

### 6.2 Manager evaluates an employee (transaction)

```mermaid
sequenceDiagram
    actor M as Manager
    participant S as EvaluationServlet
    participant SV as EvaluationService
    participant SC as ScoreCalculator
    participant TM as TransactionManager
    participant D as EvaluationDao
    participant DB as PostgreSQL

    M->>S: POST evaluation form
    S->>SV: createEvaluation(actor, ...)
    SV->>SV: validate input and check authorisation
    SV->>SV: load criteria, check team membership, check duplicate period
    SV->>SC: computeOverallScore(scores)
    SC-->>SV: score and stage
    SV->>TM: executeInTransaction(work)
    TM->>DB: setAutoCommit(false)
    TM->>D: saveWithScores(conn, evaluation, scores)
    D->>DB: INSERT evaluations
    D->>DB: INSERT evaluation_scores (one per criterion)
    alt all inserts succeed
        TM->>DB: COMMIT
    else any exception
        TM->>DB: ROLLBACK
    end
    TM-->>SV: saved evaluation or DataAccessException
    SV-->>S: Evaluation
    S-->>M: redirect to evaluation detail page
```

### 6.3 Overdue goal check (background thread)

```mermaid
sequenceDiagram
    participant SCH as GoalDeadlineScheduler (daemon thread)
    participant GS as GoalService
    participant TM as TransactionManager
    participant GD as GoalDao
    participant DB as PostgreSQL

    loop every N minutes
        SCH->>GS: markOverdueGoals(today)
        GS->>TM: executeInTransaction(work)
        TM->>GD: find open goals with target date before today
        GD->>DB: SELECT
        TM->>GD: update status to MISSED
        GD->>DB: UPDATE
        TM->>DB: COMMIT
        GS-->>SCH: count marked
        SCH->>SCH: add to totalMarkedMissed, set lastRun
    end
```

### 6.4 Report export (worker thread)

```mermaid
sequenceDiagram
    actor U as User
    participant S as ReportServlet
    participant RJ as ReportJobService
    participant W as Worker thread
    participant RS as ReportService

    U->>S: request PDF or CSV export
    S->>RJ: submit(task)
    RJ-->>S: jobId
    S-->>U: page showing status PENDING
    RJ->>W: run task
    W->>RS: build report data
    W->>W: write PDF or CSV
    U->>S: poll status(jobId)
    S->>RJ: getStatus(jobId)
    RJ-->>S: DONE
    S-->>U: download file
```

---

## 7. Goal status state diagram

```mermaid
stateDiagram-v2
    [*] --> NOT_STARTED : goal created
    NOT_STARTED --> IN_PROGRESS : progress 1 to 99
    IN_PROGRESS --> COMPLETED : progress 100
    IN_PROGRESS --> NOT_STARTED : progress set to 0
    COMPLETED --> IN_PROGRESS : progress lowered
    NOT_STARTED --> MISSED : target date passed
    IN_PROGRESS --> MISSED : target date passed
    MISSED --> [*]
    COMPLETED --> [*]
```

---

## 8. Evaluation flow (activity)

```mermaid
flowchart TD
    A([Manager opens evaluation form]) --> B["Select employee and period"]
    B --> C["Rate each criterion 1 to 5 with optional comment"]
    C --> D{"Input valid?"}
    D -- No --> E["Show all validation errors"] --> C
    D -- Yes --> F{"Employee in manager's team and period not used?"}
    F -- No --> G["Show business rule error"] --> B
    F -- Yes --> H["Compute weighted score and Rocket Stage"]
    H --> I["Begin transaction"]
    I --> J["Save evaluation and all scores"]
    J --> K{"Any error?"}
    K -- Yes --> L["Rollback and show error"]
    K -- No --> M["Commit"] --> N([Show evaluation report])
```
