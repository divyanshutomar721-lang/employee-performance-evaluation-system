package com.epes.servlet;

import com.epes.exception.BusinessRuleException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.User;
import com.epes.util.FlashMessage;
import com.epes.util.FormParams;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servlet managing user accounts, role transitions, and account activation states.
 * Restricted to administrators by {@link com.epes.filter.RoleFilter}.
 */
@WebServlet(name = "AdminUsersServlet", urlPatterns = {
        "/admin/users",
        "/admin/users/create",
        "/admin/users/role",
        "/admin/users/active"
})
public class AdminUsersServlet extends BaseServlet {

    private static final String FORM_EMAIL = "formUser_email";
    private static final String FORM_FULL_NAME = "formUser_fullName";
    private static final String FORM_ROLE = "formUser_role";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = currentUser(req);
        try {
            List<User> users = appContext().getUserService().listUsers(currentUser);
            users.sort(Comparator.comparing(User::getId));

            boolean hasOtherUsers = users.stream().anyMatch(u -> !u.getId().equals(currentUser.getId()));
            req.setAttribute("users", users);
            req.setAttribute("hasOtherUsers", hasOtherUsers);

            HttpSession session = req.getSession(false);
            if (session != null) {
                req.setAttribute("formEmail", session.getAttribute(FORM_EMAIL));
                req.setAttribute("formFullName", session.getAttribute(FORM_FULL_NAME));
                req.setAttribute("formRole", session.getAttribute(FORM_ROLE));
                session.removeAttribute(FORM_EMAIL);
                session.removeAttribute(FORM_FULL_NAME);
                session.removeAttribute(FORM_ROLE);
            }

            forward(req, resp, "admin/users");
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
                case "/admin/users/create" -> handleCreate(req, resp, currentUser);
                case "/admin/users/role" -> handleRole(req, resp, currentUser);
                case "/admin/users/active" -> handleActive(req, resp, currentUser);
                default -> redirect(req, resp, "/admin/users");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        String email = req.getParameter("email");
        String fullName = req.getParameter("fullName");
        String roleStr = req.getParameter("role");

        Role role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // invalid role handled below
            }
        }

        try {
            if (role == null) {
                throw new ValidationException("A valid role (ADMIN, MANAGER, EMPLOYEE) must be selected");
            }
            User created = appContext().getUserService().createUser(currentUser, email, fullName, role);
            flash(req, FlashMessage.Type.SUCCESS, "User " + created.getEmail() + " created successfully.");
        } catch (ValidationException ex) {
            preserveFormValues(req, email, fullName, roleStr);
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            preserveFormValues(req, email, fullName, roleStr);
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/admin/users");
    }

    private void handleRole(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long userId = FormParams.parseLong(req.getParameter("userId"), "User ID", errors);
        String roleStr = req.getParameter("role");

        Role role = null;
        if (roleStr != null && !roleStr.isBlank()) {
            try {
                role = Role.valueOf(roleStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                errors.add("Invalid role specified");
            }
        } else {
            errors.add("Role cannot be blank");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/admin/users");
            return;
        }

        try {
            appContext().getUserService().changeRole(currentUser, userId, role);
            flash(req, FlashMessage.Type.SUCCESS, "User role updated successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/admin/users");
    }

    private void handleActive(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long userId = FormParams.parseLong(req.getParameter("userId"), "User ID", errors);
        String activeStr = req.getParameter("active");
        boolean active = Boolean.parseBoolean(activeStr);

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/admin/users");
            return;
        }

        try {
            appContext().getUserService().setActive(currentUser, userId, active);
            flash(req, FlashMessage.Type.SUCCESS, active ? "User account activated successfully." : "User account deactivated successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/admin/users");
    }

    private void preserveFormValues(HttpServletRequest req, String email, String fullName, String roleStr) {
        HttpSession session = req.getSession(true);
        if (email != null) session.setAttribute(FORM_EMAIL, email);
        if (fullName != null) session.setAttribute(FORM_FULL_NAME, fullName);
        if (roleStr != null) session.setAttribute(FORM_ROLE, roleStr);
    }
}
