package com.epes.servlet;

import com.epes.config.AppConstants;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.Evaluation;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.User;
import com.epes.service.ScoreInput;
import com.epes.service.UserSummary;
import com.epes.util.EvaluationFormParser;
import com.epes.util.FlashMessage;
import com.epes.util.FormParams;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet handling performance evaluation lifecycle for managers:
 * listing evaluations, conducting evaluations with live preview, viewing evaluation details,
 * and deleting evaluations.
 */
@WebServlet(name = "ManagerEvaluationsServlet", urlPatterns = {
        "/manager/evaluations",
        "/manager/evaluations/new",
        "/manager/evaluations/create",
        "/manager/evaluations/view",
        "/manager/evaluations/delete"
})
public class ManagerEvaluationsServlet extends BaseServlet {

    private static final String FORM_EMPLOYEE_ID = "eval_form_employee_id";
    private static final String FORM_TEAM_ID = "eval_form_team_id";
    private static final String FORM_PERIOD = "eval_form_period";
    private static final String FORM_SUMMARY = "eval_form_summary";
    private static final String FORM_RATINGS = "eval_form_ratings";
    private static final String FORM_COMMENTS = "eval_form_comments";
    private static final String FORM_ERRORS = "eval_form_errors";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        User currentUser = currentUser(req);

        try {
            switch (path) {
                case "/manager/evaluations/new" -> renderNewForm(req, resp, currentUser);
                case "/manager/evaluations/view" -> renderView(req, resp, currentUser);
                default -> renderList(req, resp, currentUser);
            }
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
                case "/manager/evaluations/create" -> handleCreate(req, resp, currentUser);
                case "/manager/evaluations/delete" -> handleDelete(req, resp, currentUser);
                default -> redirect(req, resp, "/manager/evaluations");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void renderList(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<Evaluation> evaluations = appContext().getEvaluationService().listForManager(currentUser);

        Set<Long> userIds = new HashSet<>();
        for (Evaluation eval : evaluations) {
            if (eval.getEmployeeId() != null) userIds.add(eval.getEmployeeId());
            if (eval.getManagerId() != null) userIds.add(eval.getManagerId());
        }
        Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(currentUser, userIds);

        List<Team> teams = appContext().getTeamService().listMyTeams(currentUser);
        Map<Long, String> teamNames = new HashMap<>();
        for (Team t : teams) {
            teamNames.put(t.getId(), t.getName());
        }

        req.setAttribute("evaluations", evaluations);
        req.setAttribute("people", people);
        req.setAttribute("teamNames", teamNames);
        forward(req, resp, "manager/evaluations");
    }

    private void renderNewForm(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<UserSummary> employees = appContext().getTeamService().listMyEmployees(currentUser);
        List<Team> teams = appContext().getTeamService().listMyTeams(currentUser);
        List<Criterion> activeCriteria = appContext().getCriterionService().listMine(currentUser, true);

        req.setAttribute("employees", employees);
        req.setAttribute("teams", teams);
        req.setAttribute("criteria", activeCriteria);

        HttpSession session = req.getSession(false);
        if (session != null) {
            req.setAttribute("formEmployeeId", session.getAttribute(FORM_EMPLOYEE_ID));
            req.setAttribute("formTeamId", session.getAttribute(FORM_TEAM_ID));
            req.setAttribute("formPeriod", session.getAttribute(FORM_PERIOD));
            req.setAttribute("formSummary", session.getAttribute(FORM_SUMMARY));
            req.setAttribute("formRatings", session.getAttribute(FORM_RATINGS));
            req.setAttribute("formComments", session.getAttribute(FORM_COMMENTS));

            @SuppressWarnings("unchecked")
            List<String> formErrors = (List<String>) session.getAttribute(FORM_ERRORS);
            if (formErrors != null && !formErrors.isEmpty()) {
                req.setAttribute(AppConstants.REQ_VALIDATION_ERRORS, formErrors);
            }

            session.removeAttribute(FORM_EMPLOYEE_ID);
            session.removeAttribute(FORM_TEAM_ID);
            session.removeAttribute(FORM_PERIOD);
            session.removeAttribute(FORM_SUMMARY);
            session.removeAttribute(FORM_RATINGS);
            session.removeAttribute(FORM_COMMENTS);
            session.removeAttribute(FORM_ERRORS);
        }

        forward(req, resp, "manager/evaluation-form");
    }

    private void renderView(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long evalId = FormParams.parseLong(req.getParameter("id"), "Evaluation ID", errors);
        if (evalId == null) {
            throw new EntityNotFoundException("Evaluation ID is missing or invalid");
        }

        Evaluation eval = appContext().getEvaluationService().getEvaluation(currentUser, evalId);

        Set<Long> userIds = new HashSet<>();
        if (eval.getEmployeeId() != null) userIds.add(eval.getEmployeeId());
        if (eval.getManagerId() != null) userIds.add(eval.getManagerId());
        Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(currentUser, userIds);

        String teamName = "-";
        if (eval.getTeamId() != null) {
            try {
                Team team = appContext().getTeamService().getTeam(currentUser, eval.getTeamId());
                teamName = team.getName();
            } catch (Exception ignored) {
                // team may have been deleted or inaccessible
            }
        }

        req.setAttribute("evaluation", eval);
        req.setAttribute("people", people);
        req.setAttribute("teamName", teamName);
        forward(req, resp, "manager/evaluation-detail");
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee", errors);

        String teamIdStr = req.getParameter("teamId");
        Long teamId = null;
        if (teamIdStr != null && !teamIdStr.isBlank()) {
            teamId = FormParams.parseLong(teamIdStr, "Team", errors);
        }

        String period = req.getParameter("period");
        if (period == null || period.isBlank()) {
            errors.add("Period label is required (e.g. 'Q3 2026')");
        }

        String summary = req.getParameter("summary");

        List<Criterion> activeCriteria = appContext().getCriterionService().listMine(currentUser, true);
        List<Long> activeIds = activeCriteria.stream().map(Criterion::getId).toList();

        Map<Long, ScoreInput> inputs = EvaluationFormParser.parse(req::getParameter, activeIds, errors);
        if (inputs.isEmpty() && errors.isEmpty()) {
            errors.add("At least one evaluation criterion must be rated");
        }

        Map<String, String> ratingsRefill = new HashMap<>();
        Map<String, String> commentsRefill = new HashMap<>();
        for (Long cId : activeIds) {
            String r = req.getParameter("rating_" + cId);
            String c = req.getParameter("comment_" + cId);
            if (r != null) ratingsRefill.put(cId.toString(), r);
            if (c != null) commentsRefill.put(cId.toString(), c);
        }

        if (!errors.isEmpty()) {
            preserveForm(req, req.getParameter("employeeId"), teamIdStr, period, summary, ratingsRefill, commentsRefill, errors);
            redirect(req, resp, "/manager/evaluations/new");
            return;
        }

        try {
            Evaluation eval = appContext().getEvaluationService().createEvaluation(
                    currentUser, employeeId, teamId, period, summary, inputs);
            flash(req, FlashMessage.Type.SUCCESS, "Evaluation for period '" + eval.getPeriodLabel() + "' created successfully.");
            redirect(req, resp, "/manager/evaluations/view?id=" + eval.getId());
        } catch (ValidationException ex) {
            List<String> allErrors = new ArrayList<>(ex.getErrors());
            preserveForm(req, req.getParameter("employeeId"), teamIdStr, period, summary, ratingsRefill, commentsRefill, allErrors);
            redirect(req, resp, "/manager/evaluations/new");
        } catch (BusinessRuleException ex) {
            List<String> ruleErrors = List.of(ex.getMessage());
            preserveForm(req, req.getParameter("employeeId"), teamIdStr, period, summary, ratingsRefill, commentsRefill, ruleErrors);
            redirect(req, resp, "/manager/evaluations/new");
        } catch (AuthorizationException ex) {
            if (currentUser != null && currentUser.getRole() == Role.MANAGER) {
                List<String> authErrors = List.of(ex.getMessage());
                preserveForm(req, req.getParameter("employeeId"), teamIdStr, period, summary, ratingsRefill, commentsRefill, authErrors);
                redirect(req, resp, "/manager/evaluations/new");
            } else {
                handleServiceException(req, resp, ex);
            }
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long evalId = FormParams.parseLong(req.getParameter("id"), "Evaluation ID", errors);
        if (evalId == null) {
            flash(req, FlashMessage.Type.ERROR, "Invalid evaluation ID");
            redirect(req, resp, "/manager/evaluations");
            return;
        }

        appContext().getEvaluationService().deleteEvaluation(currentUser, evalId);
        flash(req, FlashMessage.Type.SUCCESS, "Evaluation deleted successfully.");
        redirect(req, resp, "/manager/evaluations");
    }

    private void preserveForm(HttpServletRequest req, String employeeId, String teamId,
                              String period, String summary,
                              Map<String, String> ratings, Map<String, String> comments,
                              List<String> errors) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FORM_EMPLOYEE_ID, employeeId);
        session.setAttribute(FORM_TEAM_ID, teamId);
        session.setAttribute(FORM_PERIOD, period);
        session.setAttribute(FORM_SUMMARY, summary);
        session.setAttribute(FORM_RATINGS, ratings);
        session.setAttribute(FORM_COMMENTS, comments);
        session.setAttribute(FORM_ERRORS, errors);
    }
}
