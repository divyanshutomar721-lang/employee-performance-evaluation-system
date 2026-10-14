package com.epes.servlet;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;
import com.epes.service.UserSummary;
import com.epes.util.FlashMessage;
import com.epes.util.FormParams;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Servlet managing organizational teams, team assignments, and member roster management.
 * Restricted to managers by {@link com.epes.filter.RoleFilter}.
 */
@WebServlet(name = "ManagerTeamsServlet", urlPatterns = {
        "/manager/teams",
        "/manager/teams/create",
        "/manager/teams/view",
        "/manager/teams/update",
        "/manager/teams/delete",
        "/manager/teams/members/add",
        "/manager/teams/members/role",
        "/manager/teams/members/remove"
})
public class ManagerTeamsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        User currentUser = currentUser(req);

        try {
            if ("/manager/teams/view".equals(path)) {
                renderTeamDetail(req, resp, currentUser);
            } else {
                renderTeamsList(req, resp, currentUser);
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void renderTeamsList(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<Team> teams = appContext().getTeamService().listMyTeams(currentUser);
        teams.sort(Comparator.comparing(Team::getId));

        List<UserSummary> assignableEmployees = appContext().getTeamService().listAssignableEmployees(currentUser);

        req.setAttribute("teams", teams);
        req.setAttribute("assignableEmployees", assignableEmployees);

        forward(req, resp, "manager/teams");
    }

    private void renderTeamDetail(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("id"), "Team ID", errors);
        if (teamId == null) {
            throw new EntityNotFoundException("Team ID is required to view team details");
        }

        Team team = appContext().getTeamService().getTeam(currentUser, teamId);

        List<TeamMember> members = team.getMembers();
        List<Long> memberIds = members.stream().map(TeamMember::getEmployeeId).toList();
        Map<Long, UserSummary> userSummaries = appContext().getUserLookupService().summaries(currentUser, memberIds);

        List<UserSummary> allAssignable = appContext().getTeamService().listAssignableEmployees(currentUser);
        Set<Long> existingMemberIds = members.stream().map(TeamMember::getEmployeeId).collect(Collectors.toSet());
        List<UserSummary> availableToAdd = allAssignable.stream()
                .filter(u -> !existingMemberIds.contains(u.getId()))
                .toList();

        req.setAttribute("team", team);
        req.setAttribute("members", members);
        req.setAttribute("userSummaries", userSummaries);
        req.setAttribute("availableToAdd", availableToAdd);

        forward(req, resp, "manager/team-detail");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        User currentUser = currentUser(req);

        try {
            switch (path) {
                case "/manager/teams/create" -> handleCreate(req, resp, currentUser);
                case "/manager/teams/update" -> handleUpdate(req, resp, currentUser);
                case "/manager/teams/delete" -> handleDelete(req, resp, currentUser);
                case "/manager/teams/members/add" -> handleAddMember(req, resp, currentUser);
                case "/manager/teams/members/role" -> handleUpdateRole(req, resp, currentUser);
                case "/manager/teams/members/remove" -> handleRemoveMember(req, resp, currentUser);
                default -> redirect(req, resp, "/manager/teams");
            }
        } catch (Exception ex) {
            handleServiceException(req, resp, ex);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        String name = req.getParameter("name");
        String description = req.getParameter("description");
        String[] employeeIdStrs = req.getParameterValues("employeeIds");

        List<String> errors = new ArrayList<>();
        List<Long> employeeIds = new ArrayList<>();
        Map<Long, String> roleByEmployeeId = new HashMap<>();

        if (employeeIdStrs != null) {
            for (String str : employeeIdStrs) {
                Long empId = FormParams.parseLong(str, "Employee ID", errors);
                if (empId != null) {
                    employeeIds.add(empId);
                    String roleInTeam = req.getParameter("role_" + empId);
                    roleByEmployeeId.put(empId, (roleInTeam != null && !roleInTeam.isBlank()) ? roleInTeam.trim() : "Member");
                }
            }
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams");
            return;
        }

        try {
            Team created = appContext().getTeamService().createTeam(currentUser, name, description, employeeIds, roleByEmployeeId);
            flash(req, FlashMessage.Type.SUCCESS, "Team \"" + created.getName() + "\" created successfully with " + created.getMembers().size() + " members.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("teamId"), "Team ID", errors);
        String name = req.getParameter("name");
        String description = req.getParameter("description");

        if (teamId == null) {
            throw new EntityNotFoundException("Team ID cannot be null");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams/view?id=" + teamId);
            return;
        }

        try {
            appContext().getTeamService().updateTeam(currentUser, teamId, name, description);
            flash(req, FlashMessage.Type.SUCCESS, "Team details updated successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams/view?id=" + teamId);
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("teamId"), "Team ID", errors);
        if (teamId == null) {
            throw new EntityNotFoundException("Team ID cannot be null");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams");
            return;
        }

        try {
            appContext().getTeamService().deleteTeam(currentUser, teamId);
            flash(req, FlashMessage.Type.SUCCESS, "Team deleted successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams");
    }

    private void handleAddMember(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("teamId"), "Team ID", errors);
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee ID", errors);
        String roleInTeam = req.getParameter("roleInTeam");

        if (teamId == null) {
            throw new EntityNotFoundException("Team ID cannot be null");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams/view?id=" + teamId);
            return;
        }

        String role = (roleInTeam != null && !roleInTeam.isBlank()) ? roleInTeam.trim() : "Member";

        try {
            appContext().getTeamService().addMember(currentUser, teamId, employeeId, role);
            flash(req, FlashMessage.Type.SUCCESS, "Member added to team.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams/view?id=" + teamId);
    }

    private void handleUpdateRole(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("teamId"), "Team ID", errors);
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee ID", errors);
        String roleInTeam = req.getParameter("roleInTeam");

        if (teamId == null) {
            throw new EntityNotFoundException("Team ID cannot be null");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams/view?id=" + teamId);
            return;
        }

        String role = (roleInTeam != null && !roleInTeam.isBlank()) ? roleInTeam.trim() : "Member";

        try {
            appContext().getTeamService().updateMemberRole(currentUser, teamId, employeeId, role);
            flash(req, FlashMessage.Type.SUCCESS, "Member role updated successfully.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams/view?id=" + teamId);
    }

    private void handleRemoveMember(HttpServletRequest req, HttpServletResponse resp, User currentUser)
            throws Exception {
        List<String> errors = new ArrayList<>();
        Long teamId = FormParams.parseLong(req.getParameter("teamId"), "Team ID", errors);
        Long employeeId = FormParams.parseLong(req.getParameter("employeeId"), "Employee ID", errors);

        if (teamId == null) {
            throw new EntityNotFoundException("Team ID cannot be null");
        }

        if (!errors.isEmpty()) {
            for (String err : errors) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
            redirect(req, resp, "/manager/teams/view?id=" + teamId);
            return;
        }

        try {
            appContext().getTeamService().removeMember(currentUser, teamId, employeeId);
            flash(req, FlashMessage.Type.SUCCESS, "Member removed from team.");
        } catch (ValidationException ex) {
            for (String err : ex.getErrors()) {
                flash(req, FlashMessage.Type.ERROR, err);
            }
        } catch (BusinessRuleException ex) {
            flash(req, FlashMessage.Type.ERROR, ex.getMessage());
        }

        redirect(req, resp, "/manager/teams/view?id=" + teamId);
    }
}
