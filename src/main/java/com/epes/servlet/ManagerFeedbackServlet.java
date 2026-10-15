package com.epes.servlet;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Evaluation;
import com.epes.model.Feedback;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet managing continuous 360 and manager feedback:
 * providing feedback with ratings, optional evaluation linking, and deleting feedback.
 */
@WebServlet(name = "ManagerFeedbackServlet", urlPatterns = {
        "/manager/feedback",
        "/manager/feedback/create",
        "/manager/feedback/delete"
})
public class ManagerFeedbackServlet extends BaseServlet {

    private static final String FORM_EMP_ID = "fb_form_emp_id";
    private static final String FORM_EVAL_ID = "fb_form_eval_id";
    private static final String FORM_COMMENTS = "fb_form_comments";
    private static final String FORM_RATING = "fb_form_rating";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = currentUser(req);

        try {
            List<Feedback> feedbackList = appContext().getFeedbackService().listGivenByMe(currentUser);
            List<UserSummary> employees = appContext().getTeamService().listMyEmployees(currentUser);
            List<Evaluation> evaluations = appContext().getEvaluationService().listForManager(currentUser);

            Set<Long> userIds = new HashSet<>();
            for (Feedback fb : feedbackList) {
                if (fb.getEmployeeId() != null) userIds.add(fb.getEmployeeId());
            }
            for (Evaluation eval : evaluations) {
                if (eval.getEmployeeId() != null) userIds.add(eval.getEmployeeId());
            }
            for (UserSummary emp : employees) {
                userIds.add(emp.getId());
            }

            Map<Long, UserSummary> people = appContext().getUserLookupService().summaries(currentUser, userIds);

            Map<Long, String> evalPeriods = new HashMap<>();
            for (Evaluation eval : evaluations) {
                evalPeriods.put(eval.getId(), eval.getPeriodLabel());
            }

            req.setAttribute("feedbackList", feedbackList);
            req.setAttribute("employees", employees);
            req.setAttribute("evaluations", evaluations);
            req.setAttribute("people", people);
            req.setAttribute("evalPeriods", evalPeriods);

            HttpSession session = req.getSession(false);
            if (session != null) {
                req.setAttribute("formEmployeeId", session.getAttribute(FORM_EMP_ID));
                req.setAttribute("formEvaluationId", session.getAttribute(FORM_EVAL_ID));
                req.setAttribute("formComments", session.getAttribute(FORM_COMMENTS));
                req.setAttribute("formRating", session.getAttribute(FORM_RATING));
                session.removeAttribute(FORM_EMP_ID);
                session.removeAttribute(FORM_EVAL_ID);
                session.removeAttribute(FORM_COMMENTS);
                session.removeAttribute(FORM_RATING);
            }

            forward(req, resp, "manager/feedback");
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
                case "/manager/feedback/create" -> handleCreate(req, resp, currentUser);
                case "/manager/feedback/delete" -> handleDelete(req, resp, currentUser);
                default -> redirect(req, resp, "/manager/feedback");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee", errors);

        String evalIdStr = req.getParameter("evaluationId");
        Long evalId = null;
        if (evalIdStr != null && !evalIdStr.isBlank()) {
            evalId = FormParams.parseLong(evalIdStr, "Linked Evaluation", errors);
        }

        String comments = req.getParameter("comments");
        if (comments == null || comments.isBlank()) {
            errors.add("Feedback comments cannot be blank");
        } else if (comments.trim().length() > 1000) {
            errors.add("Feedback comments cannot exceed 1000 characters");
        }

        Integer rating = FormParams.parseInt(req.getParameter("rating"), "Rating (1 to 5)", errors);
        if (rating != null && (rating < 1 || rating > 5)) {
            errors.add("Rating must be between 1 and 5");
        }

        if (!errors.isEmpty()) {
            preserveForm(req, req.getParameter("employeeId"), evalIdStr, comments, req.getParameter("rating"));
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/feedback");
            return;
        }

        try {
            appContext().getFeedbackService().giveFeedback(
                    currentUser, employeeId, evalId, comments.trim(), rating);
            flash(req, FlashMessage.Type.SUCCESS, "Feedback submitted successfully.");
        } catch (ValidationException ex) {
            preserveForm(req, req.getParameter("employeeId"), evalIdStr, comments, req.getParameter("rating"));
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            preserveForm(req, req.getParameter("employeeId"), evalIdStr, comments, req.getParameter("rating"));
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/feedback");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long fbId = FormParams.parseLong(req.getParameter("id"), "Feedback ID", errors);
        if (fbId == null) {
            flash(req, FlashMessage.Type.ERROR, "Invalid feedback ID");
            redirect(req, resp, "/manager/feedback");
            return;
        }

        appContext().getFeedbackService().deleteFeedback(currentUser, fbId);
        flash(req, FlashMessage.Type.SUCCESS, "Feedback record deleted.");
        redirect(req, resp, "/manager/feedback");
    }

    private void preserveForm(HttpServletRequest req, String employeeId, String evaluationId, String comments, String rating) {
        HttpSession session = req.getSession(true);
        session.setAttribute(FORM_EMP_ID, employeeId);
        session.setAttribute(FORM_EVAL_ID, evaluationId);
        session.setAttribute(FORM_COMMENTS, comments);
        session.setAttribute(FORM_RATING, rating);
    }
}
