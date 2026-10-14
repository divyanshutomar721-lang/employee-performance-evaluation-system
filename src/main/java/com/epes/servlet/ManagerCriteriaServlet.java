package com.epes.servlet;

import com.epes.exception.BusinessRuleException;
import com.epes.exception.ValidationException;
import com.epes.model.Criterion;
import com.epes.model.User;
import com.epes.util.FlashMessage;
import com.epes.util.FormParams;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servlet handling evaluation criteria creation, inline updates, deactivation, and deletion.
 * Restricted to managers by {@link com.epes.filter.RoleFilter}.
 */
@WebServlet(name = "ManagerCriteriaServlet", urlPatterns = {
        "/manager/criteria",
        "/manager/criteria/save",
        "/manager/criteria/deactivate",
        "/manager/criteria/delete"
})
public class ManagerCriteriaServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = currentUser(req);
        try {
            List<Criterion> criteria = appContext().getCriterionService().listMine(currentUser, false);
            criteria.sort(Comparator.comparing(Criterion::isActive).reversed().thenComparing(Criterion::getId));

            int totalActiveWeight = criteria.stream()
                    .filter(Criterion::isActive)
                    .mapToInt(Criterion::getWeight)
                    .sum();

            req.setAttribute("criteria", criteria);
            req.setAttribute("totalActiveWeight", totalActiveWeight);

            forward(req, resp, "manager/criteria");
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
                case "/manager/criteria/save" -> handleSave(req, resp, currentUser);
                case "/manager/criteria/deactivate" -> handleDeactivate(req, resp, currentUser);
                case "/manager/criteria/delete" -> handleDelete(req, resp, currentUser);
                default -> redirect(req, resp, "/manager/criteria");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleSave(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        String idStr = req.getParameter("id");
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String weightStr = req.getParameter("weight");

        List<String> errors = new ArrayList<>();
        Integer weight = FormParams.parseInt(weightStr, "Weight", errors);
        Long id = null;
        if (idStr != null && !idStr.isBlank()) {
            id = FormParams.parseLong(idStr, "Criterion ID", errors);
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/criteria");
            return;
        }

        try {
            if (id == null) {
                appContext().getCriterionService().create(currentUser, name, description, weight != null ? weight : 0);
                flash(req, FlashMessage.Type.SUCCESS, "Criterion created successfully.");
            } else {
                appContext().getCriterionService().update(currentUser, id, name, description, weight != null ? weight : 0);
                flash(req, FlashMessage.Type.SUCCESS, "Criterion updated successfully.");
            }
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/criteria");
    }

    private void handleDeactivate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long id = FormParams.parseLong(req.getParameter("id"), "Criterion ID", errors);

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/criteria");
            return;
        }

        try {
            appContext().getCriterionService().deactivate(currentUser, id);
            flash(req, FlashMessage.Type.SUCCESS, "Criterion deactivated.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/criteria");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long id = FormParams.parseLong(req.getParameter("id"), "Criterion ID", errors);

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/criteria");
            return;
        }

        try {
            appContext().getCriterionService().delete(currentUser, id);
            flash(req, FlashMessage.Type.SUCCESS, "Criterion deleted successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/criteria");
    }
}
