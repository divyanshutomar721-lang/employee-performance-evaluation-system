# Documentation: Employee Performance Evaluation System (EPES)

**Project:** College project 18, Employee Performance Evaluation System (Java web application)
**Student:** Divyanshu Tomar 
**Course submission:** Review 1 (Pre-Midterm Submission)

## Documents

| # | Document | What it contains |
|---|---|---|
| 1 | [Requirement Analysis](01-requirement-analysis.md) | Problem statement, scope, roles, functional requirements with input and output, business rules, Rocket Stage rating model, non-functional requirements, traceability |
| 2 | [Design Diagrams](02-design-diagrams.md) | Use case, architecture, ER, class diagrams, sequence diagrams, state diagram, flow chart |
| 3 | [Solution Design and Logical Plan](03-solution-design.md) | Layers, packages, patterns, Java concepts used, database and transaction design, security, threads, phase plan, rubric mapping |
| 4 | [User Guide (Phase 4A)](04-user-guide.md) | Admin user management, manager criteria, manager teams and members |
| 5 | [Database Dictionary](../db/README.md) | Every table, column, key and relationship |
| 6 | [Project README](../README.md) | Setup, run, test commands |

The diagrams use Mermaid, so GitHub shows them directly in the browser.

## Where to find the Review 1 marks in this repository

| Deliverable | Marks | Look at |
|---|---|---|
| Problem Understanding and Solution Design | 8 | Documents 1, 2 and 3 above |
| Core Java Concepts (OOP, Collections, Exceptions, Threads) | 10 | `src/main/java/com/epes/model`, `service`, `exception`, `concurrent` and section 4 of document 3 |
| Database Integration (JDBC) | 8 | `src/main/resources/db/schema.sql`, `src/main/java/com/epes/dao`, `util/TransactionManager.java`, tests in `src/test/java/com/epes/dao` |
| Servlets and Web Integration | 7 | `src/main/java/com/epes/servlet`, `filter`, `listener`, `src/main/webapp` |

## How to run

```
git clone https://github.com/divyanshutomar721-lang/employee-performance-evaluation-system.git
cd employee-performance-evaluation-system
psql -U postgres -h localhost -c "CREATE DATABASE epes;"
set DB_PASSWORD=your_database_password      (Windows cmd)
mvn clean package
mvn cargo:run
```

Open http://localhost:8080/epes/ and http://localhost:8080/epes/health.
See the project README for all environment variables.
