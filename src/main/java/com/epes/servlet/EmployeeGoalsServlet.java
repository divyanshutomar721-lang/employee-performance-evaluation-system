package com.epes.servlet;

import com.epes.exception.ValidationException;
import com.epes.model.Goal;
import com.epes.model.GoalType;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.util.FlashMessage;
import jakarta.servlet.ServletException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet managing employee goals, supporting personal goal creation, progress updates,
 * inline editing of personal goals, and deletion restrictions on assigned goals.
 */
@WebServlet(name = "EmployeeGoalsServlet", urlPatterns = {
        "/employee/goals",
        "/employee/goals/create",
        "/employee/goals/progress",
        "/employee/goals/edit",
        "/employee/goals/delete"
})
public class EmployeeGoalsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        req.setAttribute("user", user);

        try {
            List<Goal> goals = appContext().getGoalService().listForEmployee(user, user.getId());
            req.setAttribute("goals", goals);

            Set<Long> setByIds = new HashSet<>();
            for (Goal g : goals) {
                if (g.getSetBy() != null && !g.getSetBy().equals(user.getId())) {
                    setByIds.add(g.getSetBy());
                }
            }

            Map<Long, UserSummary> managers = appContext().getUserLookupService().summaries(user, setByIds);
            req.setAttribute("managers", managers);
            req.setAttribute("today", LocalDate.now());
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
            return;
        }

        forward(req, resp, "employee/goals");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User user = currentUser(req);
        String path = req.getServletPath();

        try {
            switch (path) {
                case "/employee/goals/create" -> handleCreate(req, resp, user);
                case "/employee/goals/progress" -> handleProgress(req, resp, user);
                case "/employee/goals/edit" -> handleEdit(req, resp, user);
                case "/employee/goals/delete" -> handleDelete(req, resp, user);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String description = req.getParameter("description");
        String targetDateStr = req.getParameter("targetDate");

        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Goal description is required");
        }
        if (description.length() > 500) {
            throw new ValidationException("Goal description must not exceed 500 characters");
        }
        if (targetDateStr == null || targetDateStr.trim().isEmpty()) {
            throw new ValidationException("Target date is required");
        }

        LocalDate targetDate;
        try {
            targetDate = LocalDate.parse(targetDateStr.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid target date format");
        }

        appContext().getGoalService().createPersonalGoal(user, description.trim(), targetDate);
        flash(req, FlashMessage.Type.SUCCESS, "Personal goal created successfully.");
        redirect(req, resp, "/employee/goals");
    }

    private void handleProgress(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String idStr = req.getParameter("id");
        String progressStr = req.getParameter("progress");

        if (idStr == null || idStr.trim().isEmpty()) {
            throw new ValidationException("Goal ID is required");
        }
        if (progressStr == null || progressStr.trim().isEmpty()) {
            throw new ValidationException("Progress percentage is required");
        }

        Long goalId = Long.valueOf(idStr.trim());
        int progress = Integer.parseInt(progressStr.trim());

        appContext().getGoalService().updateProgress(user, goalId, progress);
        flash(req, FlashMessage.Type.SUCCESS, "Goal progress updated.");
        redirect(req, resp, "/employee/goals");
    }

    private void handleEdit(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String idStr = req.getParameter("id");
        String description = req.getParameter("description");
        String targetDateStr = req.getParameter("targetDate");

        if (idStr == null || idStr.trim().isEmpty()) {
            throw new ValidationException("Goal ID is required");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Goal description is required");
        }
        if (description.length() > 500) {
            throw new ValidationException("Goal description must not exceed 500 characters");
        }
        if (targetDateStr == null || targetDateStr.trim().isEmpty()) {
            throw new ValidationException("Target date is required");
        }

        Long goalId = Long.valueOf(idStr.trim());
        LocalDate targetDate = LocalDate.parse(targetDateStr.trim());

        appContext().getGoalService().editGoal(user, goalId, description.trim(), targetDate);
        flash(req, FlashMessage.Type.SUCCESS, "Goal updated successfully.");
        redirect(req, resp, "/employee/goals");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User user)
            throws Exception {
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.trim().isEmpty()) {
            throw new ValidationException("Goal ID is required");
        }
        Long goalId = Long.valueOf(idStr.trim());

        appContext().getGoalService().deleteGoal(user, goalId);
        flash(req, FlashMessage.Type.SUCCESS, "Goal deleted successfully.");
        redirect(req, resp, "/employee/goals");
    }
}

