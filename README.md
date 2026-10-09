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
| Database   | PostgreSQL (plain JDBC, DAO pattern)                  |
| Frontend   | JSP + Bootstrap 5 (CDN) + Chart.js (CDN — later)     |
| Logging    | `java.util.logging`                                   |

## Prerequisites

- **JDK 17 or later** (JDK 24 tested)
- **Apache Maven 3.9+**
- **PostgreSQL** (running, with a database called `epes` created)

## Setup

1. **Create the database** (if not already done):
   ```sql
   CREATE DATABASE epes;
   ```

2. **Set the `DB_PASSWORD` environment variable** for your PostgreSQL user:
   ```bash
   # Linux / macOS
   export DB_PASSWORD=your_password

   # Windows (PowerShell)
   $env:DB_PASSWORD = "your_password"
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

## Project Structure

```
src/main/java/com/epes/
  config/       — DatabaseConfig, AppConstants
  model/        — domain objects
  dao/          — DAO interfaces
  dao/impl/     — JDBC DAO implementations
  service/      — business logic
  exception/    — custom exceptions
  servlet/      — HTTP servlets
  filter/       — servlet filters
  util/         — ConnectionFactory, helpers

src/main/webapp/
  index.jsp
  WEB-INF/web.xml
  WEB-INF/views/
  assets/css/  assets/js/

db/             — SQL schema scripts
docs/           — documentation
```

## Project Status

- [x] **Phase 0** — Maven WAR skeleton, config, health check
- [ ] Phase 1 — Database schema, models, DAO layer
- [ ] Phase 2 — Authentication & authorisation
- [ ] Phase 3 — Core features (evaluations, goals, feedback)
- [ ] Phase 4 — Reports & dashboards
