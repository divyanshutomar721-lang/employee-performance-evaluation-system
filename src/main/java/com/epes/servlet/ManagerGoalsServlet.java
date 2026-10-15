package com.epes.servlet;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.util.FlashMessage;
import com.epes.util.FormParams;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet managing goals assigned by managers to employees:
 * assigning new goals, tracking/updating progress percentage, editing goal definitions,
 * and deleting assigned goals.
 */
@WebServlet(name = "ManagerGoalsServlet", urlPatterns = {
        "/manager/goals",
        "/manager/goals/assign",
        "/manager/goals/progress",
        "/manager/goals/edit",
        "/manager/goals/delete"
})
public class ManagerGoalsServlet extends BaseServlet {

    private static final String FORM_EMP_ID = "goal_form_emp_id";
    private static final String FORM_DESC = "goal_form_desc";
    private static final String FORM_TARGET_DATE = "goal_form_target_date";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = currentUser(req);

        try {
            List<Goal> goals = appContext().getGoalService().listAssignedByMe(currentUser);
            List<UserSummary> employees = appContext().getTeamService().listMyEmployees(currentUser);

            Set<Long> userIds = new HashSet<>();
            for (Goal g : goals) {
                if (g.getEmployeeId() != null) userIds.add(g.getEmployeeId());
            }
            Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(currentUser, userIds);

            req.setAttribute("goals", goals);
            req.setAttribute("employees", employees);
            req.setAttribute("people", people);
            req.setAttribute("today", LocalDate.now());

            HttpSession session = req.getSession(false);
            if (session != null) {
                req.setAttribute("formEmployeeId", session.getAttribute(FORM_EMP_ID));
                req.setAttribute("formDescription", session.getAttribute(FORM_DESC));
                req.setAttribute("formTargetDate", session.getAttribute(FORM_TARGET_DATE));
                session.removeAttribute(FORM_EMP_ID);
                session.removeAttribute(FORM_DESC);
                session.removeAttribute(FORM_TARGET_DATE);
            }

            forward(req, resp, "manager/goals");
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        User currentUser = currentUser(req);

        try {
            switch (path) {
                case "/manager/goals/assign" -> handleAssign(req, resp, currentUser);
                case "/manager/goals/progress" -> handleProgress(req, resp, currentUser);
                case "/manager/goals/edit" -> handleEdit(req, resp, currentUser);
                case "/manager/goals/delete" -> handleDelete(req, resp, currentUser);
                default -> redirect(req, resp, "/manager/goals");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleAssign(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee", errors);
        String description = req.getParameter("description");
        LocalDate targetDate = FormParams.parseLocalDate(req.getParameter("targetDate"), "Target Date", errors);

        if (description == null || description.isBlank()) {
            errors.add("Goal description cannot be blank");
        } else if (description.trim().length() > 500) {
            errors.add("Goal description cannot exceed 500 characters");
        }

        if (!errors.isEmpty()) {
            preserveForm(req, req.getParameter("employeeId"), description, req.getParameter("targetDate"));
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/goals");
            return;
        }

        try {
            appContext().getGoalService().assignGoal(currentUser, employeeId, description.trim(), targetDate);
            flash(req, FlashMessage.Type.SUCCESS, "Goal assigned successfully.");
        } catch (ValidationException ex) {
            preserveForm(req, req.getParameter("employeeId"), description, req.getParameter("targetDate"));
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            preserveForm(req, req.getParameter("employeeId"), description, req.getParameter("targetDate"));
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/goals");
    }

    private void handleProgress(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long goalId = FormParams.parseLong(req.getParameter("id"), "Goal ID", errors);
        Integer progress = FormParams.parseInt(req.getParameter("progress"), "Progress percentage", errors);

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/goals");
            return;
        }

        try {
            Goal updated = appContext().getGoalService().updateProgress(currentUser, goalId, progress);
            flash(req, FlashMessage.Type.SUCCESS, "Goal progress updated to " + updated.getProgressPercent() + "% (" + updated.getStatus() + ").");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/goals");
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long goalId = FormParams.parseLong(req.getParameter("id"), "Goal ID", errors);
        String description = req.getParameter("description");
        LocalDate targetDate = FormParams.parseLocalDate(req.getParameter("targetDate"), "Target Date", errors);

        if (description == null || description.isBlank()) {
            errors.add("Goal description cannot be blank");
        } else if (description.trim().length() > 500) {
            errors.add("Goal description cannot exceed 500 characters");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/goals");
            return;
        }

        try {
            appContext().getGoalService().editGoal(currentUser, goalId, description.trim(), targetDate);
            flash(req, FlashMessage.Type.SUCCESS, "Goal updated successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/goals");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long goalId = FormParams.parseLong(req.getParameter("id"), "Goal ID", errors);
        if (goalId == null) {
            flash(req, FlashMessage.Type.ERROR, "Invalid goal ID");
            redirect(req, resp, "/manager/goals");
            return;
        }

        appContext().getGoalService().deleteGoal(currentUser, goalId);
        flash(req, FlashMessage.Type.SUCCESS, "Goal deleted successfully.");
        redirect(req, resp, "/manager/goals");
    }

    private void preserveForm(HttpServletRequest req, String employeeId, String description, String targetDate) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FORM_EMP_ID, employeeId);
        session.setAttribute(FORM_DESC, description);
        session.setAttribute(FORM_TARGET_DATE, targetDate);
    }
}
