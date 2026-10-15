# User Guide: Admin and Manager Setup (Phase 4A)

This guide walks administrators and managers through setting up users, evaluation criteria, and teams in the Employee Performance Evaluation System (EPES).

---

## Admin: Managing Users

Administrators manage system user accounts, grant roles, and handle account activation.

### 1. View User Directory
1. Log in to EPES using your administrator credentials.
2. From the navigation bar or the dashboard, click **Users** (or navigate to `/admin/users`).
3. View the list of all registered users with their full name, email, role badge, active status, and creation date.

### 2. Create a New User
1. At the top of the **User Directory** page, locate the **Create New User** form.
2. Enter the user's **Email Address** (must be unique).
3. Enter the user's **Full Name**.
4. Select the user's **System Role**:
   - `ADMIN`: Full administrative privileges.
   - `MANAGER`: Evaluates employees, sets criteria, and manages teams.
   - `EMPLOYEE`: Team member evaluated by managers.
5. Click **Create User**. The new user will appear in the directory table.

### 3. Change a User's Role
1. Locate the user in the directory table.
2. In the **Role** column, select the new role from the dropdown menu.
3. Click **Save**.
4. *Note: Administrators cannot change their own role or remove the last active administrator.*

### 4. Activate or Deactivate an Account
1. Locate the user in the directory table.
2. In the **Actions** column, click **Deactivate** to disable an active account, or **Activate** to re-enable a deactivated account.
3. *Note: Administrators cannot deactivate their own active account.*

---

## Manager: Criteria

Managers define weighted evaluation criteria used during performance evaluation cycles.

### 1. View Criteria
1. Log in to EPES as a manager.
2. From the navigation bar or the dashboard, click **Criteria** (or navigate to `/manager/criteria`).
3. View the table of your evaluation criteria along with their descriptions, weights, and active status.
4. Review the **Total Active Weight** indicator above the table. For standard evaluation cycles, active weights should sum to 100%.

### 2. Create an Evaluation Criterion
1. Locate the **Add Evaluation Criterion** form at the top of the page.
2. Enter a unique **Criterion Name** (e.g., *Technical Execution*, *Leadership & Teamwork*).
3. Enter a helpful **Description** explaining what will be assessed.
4. Enter a **Weight (1 to 100)** representing the criterion's percentage in evaluations.
5. Click **Save Criterion**.

### 3. Edit an Existing Criterion
1. In the criteria table, locate the criterion you wish to update.
2. Click **Edit**.
3. In the edit modal, modify the name, description, or weight.
4. Click **Save Changes**.
5. *Note: Evaluation criteria are snapshotted into evaluations upon creation, ensuring past evaluations remain unchanged.*

### 4. Deactivate or Delete a Criterion
1. To temporarily disable a criterion without losing history, click **Deactivate** in the criteria table.
2. To permanently delete a criterion that is no longer needed, click **Delete** and confirm the browser prompt.

---

## Manager: Teams

Managers organize employees into teams and assign team-specific roles.

### 1. View Teams Overview
1. Log in to EPES as a manager.
2. From the navigation bar or the dashboard, click **Teams** (or navigate to `/manager/teams`).
3. Browse existing managed teams showing the team name, description, member count, and direct link to open details.

### 2. Create a New Team
1. In the **Create New Team** section, enter the **Team Name** and a concise **Description**.
2. In the **Assign Initial Members** section, select active employees using the checkboxes.
3. Specify each selected employee's **Role in Team** (defaults to `Member`, or customize e.g. `Tech Lead`, `Senior Engineer`).
4. Click **Create Team**.

### 3. Manage Team Details and Roster
1. From the teams list, click **Open** on the desired team.
2. **Update Details**: Modify the team name or description in the edit card and click **Save Details**.
3. **Change Member Role**: In the team roster, update the role input next to an employee and click **Update**.
4. **Remove a Member**: Click **Remove** next to a member to unassign them from the team.
5. **Add Additional Members**: Under **Add Member to Team**, choose from available unassigned employees, enter their team role, and click **Add Member**.
6. **Delete Team**: Click **Delete Team** and confirm the browser warning to delete the team and clear its member associations.

---

## Manager: Evaluating an Employee

Managers evaluate direct reports on team performance cycles, computing overall scores and assigning performance tiers (Rocket Stages).

### 1. Understanding Ratings, Formula, and Rocket Stages
- **Criterion Ratings**: Managers assign integer ratings from 1 to 5 per active criterion:
  - `1`: Launchpad (Foundational)
  - `2`: Liftoff (Developing)
  - `3`: Orbit (Proficient / Expected Standard)
  - `4`: Cruise (Exceeding Expectations)
  - `5`: Summit (Exceptional Leadership / Mastery)
- **Score Calculation Formula**:
  $$\text{Overall Score} = \left(\frac{\sum (\text{rating} \times \text{weight})}{5 \times \sum \text{weight}}\right) \times 100$$
  *Worked Example*: An evaluation with two criteria:
  - Criterion A: Weight 60, Rating 4
  - Criterion B: Weight 40, Rating 5
  - Weighted sum: $(4 \times 60) + (5 \times 40) = 240 + 200 = 440$
  - Maximum possible: $5 \times (60 + 40) = 5 \times 100 = 500$
  - Overall Score: $(440 / 500) \times 100 = 88.00\%$
  - Corresponding Rocket Stage: **Cruise**
- **Rocket Stage Thresholds**:
  - `Launchpad`: Score $< 40.00$
  - `Liftoff`: Score $40.00$ to $< 60.00$
  - `Orbit`: Score $60.00$ to $< 75.00$
  - `Cruise`: Score $75.00$ to $< 90.00$
  - `Summit`: Score $\ge 90.00$

### 2. Conduct a New Performance Evaluation
1. From the navigation bar, click **Evaluations** (or go to `/manager/evaluations`).
2. Click **New Evaluation** (or navigate to `/manager/evaluations/new`).
3. Select an evaluated **Employee** from your managed direct reports.
4. (Optional) Select the associated **Team**.
5. Enter the **Evaluation Period** label (e.g., `Q3 2026`).
6. Enter general **Summary Remarks**.
7. In the criteria scoring section, select a rating (1 to 5) for each criterion and enter optional specific comments.
8. Watch the **Live Preview** banner automatically update with the projected overall score and Rocket Stage.
9. Click **Submit Evaluation**. Upon saving, criteria names and weights are permanently snapshotted.

### 3. Review and Delete Evaluations
1. On the **Performance Evaluations** page (`/manager/evaluations`), locate the evaluation row.
2. Click **Open** to view the full evaluation breakdown, snapshot weights, ratings, and feedback remarks.
3. To delete an evaluation, click **Delete Evaluation** on the detail page and confirm the prompt.

---

## Manager: Goals

Managers assign measurable goals to employees and track milestones toward completion.

### 1. Assign a New Goal
1. From the navigation bar, click **Goals** (or navigate to `/manager/goals`).
2. In the **Assign Goal to Employee** form, select the target employee.
3. Choose a future **Target Completion Date** (past dates are rejected).
4. Enter a clear **Goal Description** (up to 500 characters).
5. Click **Assign Goal**. The goal appears in the roster with initial status `NOT STARTED` (0% progress).

### 2. Update Goal Progress
1. Locate the employee's goal in the **Assigned Goals Roster**.
2. In the **Progress** column, enter the new progress percentage (0 to 100) and click **Update**.
3. Status transitions automatically:
   - `0%`: `NOT STARTED`
   - `1% - 99%`: `IN PROGRESS`
   - `100%`: `COMPLETED`
4. Goals whose target completion date has passed without reaching 100% display a red **Overdue** badge.

### 3. Edit and Delete Goals
1. Click **Edit** on any assigned goal to adjust its description or target date.
2. Click **Delete** and confirm the prompt to permanently remove a goal.

---

## Manager: Feedback

Managers provide continuous qualitative 360 feedback and ratings outside formal evaluation cycles.

### 1. Submit Continuous Feedback
1. From the navigation bar, click **Feedback** (or navigate to `/manager/feedback`).
2. In the **Give Feedback to Employee** form, select the employee recipient.
3. (Optional) Link the feedback to an existing evaluation period, or select *Not linked to an evaluation*.
4. Select a performance rating from **1 (Launchpad)** to **5 (Summit)**.
5. Enter constructive **Feedback Comments** (up to 1000 characters).
6. Click **Submit Feedback**.

### 2. Manage Feedback Records
1. In the **Feedback Given Roster**, review all feedback you have submitted, along with recipient, rating badge, linked evaluation period, and submission date.
2. To remove a feedback entry, click the **Delete** (trash) icon and confirm the prompt.

---

## Employee: Dashboard and Progress Chart

1. Log in with your employee email. You will land directly on `/employee/dashboard`.
2. Inspect the top KPI cards: current Rocket Stage badge, latest review score, trajectory trend (Improving, Stable, Declining), and goal status counts.
3. Review the **Performance Score Progression** line chart visualizing score evolution across review cycles (scaled 0 to 100).
4. Review recent feedback submitted by managers in the right-hand panel.
5. Use quick navigation cards to jump directly to Performance, Feedback, Goals, or Reports.

---

## Employee: Feedback History

1. In the top navigation bar, click **Feedback** (or navigate to `/employee/feedback`).
2. Review the **Average Feedback Rating** KPI card at the top.
3. In the feedback history table, review each review entry: manager name, numerical rating (1 to 5) with Rocket Stage tier label, qualitative feedback comments, linked evaluation period, and timestamp.

---

## Employee: Goals

1. In the top navigation bar, click **Goals** (or navigate to `/employee/goals`).
2. Review the goal list showing both personal goals and manager-assigned goals.
3. **Create a personal goal**: In the *Create Personal Goal* card, enter a description and choose a target date today or in the future. Click **Create Goal**.
4. **Update progress**: For any goal (personal or assigned), enter a new percentage (0 to 100) in the progress field and click **Update**.
5. **Edit or delete personal goals**: Click **Edit** or **Delete** on your personal goals. Note that manager-assigned goals cannot be edited or deleted by employees.

---

## Employee: Reports and Printing

1. In the top navigation bar, click **Reports** (or navigate to `/employee/reports`).
2. Review the 6 summary KPI cards: Average Score, Latest Score, Current Stage, Trajectory, Total Evaluations, and Feedback Average.
3. Examine the 3 visual analytics charts:
   - **Performance Score Progression** line chart (0 to 100)
   - **Goal Status Breakdown** doughnut chart
   - **Average Rating per Criterion** bar chart (0 to 5)
4. Below the criterion chart, view the table of average ratings per criterion across all your evaluation cycles.
5. Click **Print this page** to generate a clean physical printout or PDF report with navigation and buttons automatically hidden.

---

## Manager: Reports

1. In the manager navigation bar, click **Reports** (or navigate to `/manager/reports`).
2. Review the manager overview cards: managed team count, distinct employee count, and composite team average score.
3. In the Managed Teams table, click **Open report** on any team to navigate to `/manager/reports/team?id=N`.
4. On the Team Report page:
   - Review the team's average performance score and the **Rocket Stage Distribution** bar chart.
   - Inspect the ranked team members table sorted by average score (un-evaluated members listed last).
5. Click **Employee report** next to any team member to open the employee drilldown report (`/manager/reports/employee?id=N`) with longitudinal charts and criterion breakdown.
6. Click **Print Report** on any report view for a printer-optimized printout.

