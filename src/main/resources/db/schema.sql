-- =====================================================================
-- Employee Performance Evaluation System (EPES)
-- Database Schema Definition (PostgreSQL)
-- Idempotent script: safe to execute multiple times.
-- =====================================================================

-- 1. Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'EMPLOYEE')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 2. Teams table
CREATE TABLE IF NOT EXISTS teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    manager_id BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT now()
);

-- 3. Team Members junction table
CREATE TABLE IF NOT EXISTS team_members (
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES users(id),
    role_in_team VARCHAR(100) NOT NULL,
    joined_at TIMESTAMP DEFAULT now(),
    PRIMARY KEY (team_id, employee_id)
);

-- 4. Criteria table (manager-defined evaluation metrics)
CREATE TABLE IF NOT EXISTS criteria (
    id BIGSERIAL PRIMARY KEY,
    manager_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    weight INT NOT NULL CHECK (weight BETWEEN 1 AND 100),
    active BOOLEAN DEFAULT TRUE,
    UNIQUE (manager_id, name)
);

-- 5. Evaluations table
CREATE TABLE IF NOT EXISTS evaluations (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES users(id),
    manager_id BIGINT NOT NULL REFERENCES users(id),
    team_id BIGINT NULL REFERENCES teams(id) ON DELETE SET NULL,
    period_label VARCHAR(100) NOT NULL,
    overall_score NUMERIC(5,2) NOT NULL CHECK (overall_score BETWEEN 0 AND 100),
    overall_stage VARCHAR(20) NOT NULL CHECK (overall_stage IN ('LAUNCHPAD', 'LIFTOFF', 'ORBIT', 'CRUISE', 'SUMMIT')),
    summary_comment TEXT,
    created_at TIMESTAMP DEFAULT now()
);

-- 6. Evaluation Scores table (snapshot of individual criterion ratings)
CREATE TABLE IF NOT EXISTS evaluation_scores (
    id BIGSERIAL PRIMARY KEY,
    evaluation_id BIGINT NOT NULL REFERENCES evaluations(id) ON DELETE CASCADE,
    criterion_id BIGINT NULL REFERENCES criteria(id) ON DELETE SET NULL,
    criterion_name VARCHAR(100) NOT NULL,
    weight INT NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT
);

-- 7. Goals table
CREATE TABLE IF NOT EXISTS goals (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES users(id),
    set_by BIGINT NOT NULL REFERENCES users(id),
    goal_type VARCHAR(20) NOT NULL CHECK (goal_type IN ('MANAGER_ASSIGNED', 'PERSONAL')),
    description TEXT NOT NULL,
    target_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'MISSED')),
    progress_percent SMALLINT NOT NULL DEFAULT 0 CHECK (progress_percent BETWEEN 0 AND 100),
    created_at TIMESTAMP DEFAULT now(),
    updated_at TIMESTAMP DEFAULT now()
);

-- 8. Feedback table
CREATE TABLE IF NOT EXISTS feedback (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES users(id),
    manager_id BIGINT NOT NULL REFERENCES users(id),
    evaluation_id BIGINT NULL REFERENCES evaluations(id) ON DELETE SET NULL,
    comments TEXT NOT NULL,
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP DEFAULT now()
);

-- Indexes for foreign keys and frequent query paths
CREATE INDEX IF NOT EXISTS idx_teams_manager_id ON teams(manager_id);
CREATE INDEX IF NOT EXISTS idx_team_members_employee_id ON team_members(employee_id);
CREATE INDEX IF NOT EXISTS idx_criteria_manager_id ON criteria(manager_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_employee_id ON evaluations(employee_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_manager_id ON evaluations(manager_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_team_id ON evaluations(team_id);
CREATE INDEX IF NOT EXISTS idx_evaluations_emp_created ON evaluations(employee_id, created_at);
CREATE INDEX IF NOT EXISTS idx_evaluation_scores_evaluation_id ON evaluation_scores(evaluation_id);
CREATE INDEX IF NOT EXISTS idx_evaluation_scores_criterion_id ON evaluation_scores(criterion_id);
CREATE INDEX IF NOT EXISTS idx_goals_employee_id ON goals(employee_id);
CREATE INDEX IF NOT EXISTS idx_goals_set_by ON goals(set_by);
CREATE INDEX IF NOT EXISTS idx_feedback_employee_id ON feedback(employee_id);
CREATE INDEX IF NOT EXISTS idx_feedback_manager_id ON feedback(manager_id);
CREATE INDEX IF NOT EXISTS idx_feedback_evaluation_id ON feedback(evaluation_id);
