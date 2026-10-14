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
