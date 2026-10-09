package com.epes.service.impl;

import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;
import com.epes.service.TeamService;
import com.epes.util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link TeamService} managing organizational teams and memberships.
 */
public class TeamServiceImpl implements TeamService {

    private final TeamDao teamDao;
    private final UserDao userDao;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if DAOs fail initialization
     */
    public TeamServiceImpl() throws DataAccessException {
        this(new TeamDaoImpl(), new UserDaoImpl());
    }

    /**
     * Parameterized constructor.
     *
     * @param teamDao team DAO
     * @param userDao user DAO
     */
    public TeamServiceImpl(TeamDao teamDao, UserDao userDao) {
        this.teamDao = teamDao;
        this.userDao = userDao;
    }

    @Override
    public Team createTeam(User actor, String name, String description,
                          List<Long> employeeIds, Map<Long, String> roleByEmployeeId)
            throws AuthorizationException, ValidationException, BusinessRuleException, DataAccessException {
        requireManager(actor);

        var collector = Validator.newCollector();
        collector.checkNotBlank(name, "Team name")
                .checkMaxLength(name, 150, "Team name");
        collector.throwIfAny();

        List<TeamMember> members = new ArrayList<>();
        if (employeeIds != null) {
            for (Long empId : employeeIds) {
                User emp = userDao.findById(empId)
                        .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + empId));
                if (!emp.isActive() || emp.getRole() != Role.EMPLOYEE) {
                    throw new BusinessRuleException("User " + emp.getFullName() + " (ID: " + empId + ") must be an active EMPLOYEE to join a team");
                }
                String roleInTeam = (roleByEmployeeId != null && roleByEmployeeId.get(empId) != null && !roleByEmployeeId.get(empId).isBlank())
                        ? roleByEmployeeId.get(empId).trim()
                        : "Member";
                members.add(new TeamMember(null, empId, roleInTeam, LocalDateTime.now()));
            }
        }

        Team team = new Team(null, name.trim(), description, actor.getId(), LocalDateTime.now());
        try {
            return teamDao.createWithMembers(team, members);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("A team with this name already exists: " + name);
        }
    }

    @Override
    public Team updateTeam(User actor, Long teamId, String name, String description)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException {
        requireManager(actor);
        Team team = requireTeamOwner(actor, teamId);

        var collector = Validator.newCollector();
        collector.checkNotBlank(name, "Team name")
                .checkMaxLength(name, 150, "Team name");
        collector.throwIfAny();

        team.setName(name.trim());
        team.setDescription(description);

        try {
            return teamDao.update(team);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("A team with this name already exists: " + name);
        }
    }

    @Override
    public void deleteTeam(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        requireManager(actor);
        requireTeamOwner(actor, teamId);
        teamDao.delete(teamId);
    }

    @Override
    public TeamMember addMember(User actor, Long teamId, Long employeeId, String roleInTeam)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException {
        requireManager(actor);
        requireTeamOwner(actor, teamId);

        if (employeeId == null) {
            throw new ValidationException("Employee ID cannot be null");
        }

        User emp = userDao.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));
        if (!emp.isActive() || emp.getRole() != Role.EMPLOYEE) {
            throw new BusinessRuleException("User " + emp.getFullName() + " must be an active EMPLOYEE to join a team");
        }

        String assignedRole = (roleInTeam != null && !roleInTeam.isBlank()) ? roleInTeam.trim() : "Member";
        TeamMember member = new TeamMember(teamId, employeeId, assignedRole, LocalDateTime.now());
        try {
            return teamDao.addMember(member);
        } catch (DuplicateEntityException e) {
            throw new ValidationException("Employee is already a member of this team");
        }
    }

    @Override
    public void updateMemberRole(User actor, Long teamId, Long employeeId, String roleInTeam)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException {
        requireManager(actor);
        requireTeamOwner(actor, teamId);

        var collector = Validator.newCollector();
        collector.checkNotNull(employeeId, "Employee ID")
                .checkNotBlank(roleInTeam, "Role in team")
                .checkMaxLength(roleInTeam, 100, "Role in team");
        collector.throwIfAny();

        teamDao.updateMemberRole(teamId, employeeId, roleInTeam.trim());
    }

    @Override
    public void removeMember(User actor, Long teamId, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        requireManager(actor);
        requireTeamOwner(actor, teamId);
        teamDao.removeMember(teamId, employeeId);
    }

    @Override
    public Team getTeam(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        Team team = teamDao.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isOwner = team.getManagerId().equals(actor.getId());
        boolean isMember = team.getMembers().stream().anyMatch(m -> m.getEmployeeId().equals(actor.getId()));

        if (!isAdmin && !isOwner && !isMember) {
            throw new AuthorizationException("You do not have permission to view this team");
        }
        return team;
    }

    @Override
    public List<Team> listMyTeams(User actor) throws AuthorizationException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (actor.getRole() == Role.MANAGER) {
            return teamDao.findByManager(actor.getId());
        } else if (actor.getRole() == Role.EMPLOYEE) {
            return teamDao.findByEmployee(actor.getId());
        } else if (actor.getRole() == Role.ADMIN) {
            // Admins can see all teams or manager-based
            return teamDao.findByManager(actor.getId());
        }
        return Collections.emptyList();
    }

    private void requireManager(User actor) {
        if (actor == null || actor.getRole() != Role.MANAGER) {
            throw new AuthorizationException("Only managers can perform this action");
        }
    }

    private Team requireTeamOwner(User actor, Long teamId) throws DataAccessException {
        if (teamId == null) {
            throw new ValidationException("Team ID cannot be null");
        }
        Team team = teamDao.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId));
        if (!team.getManagerId().equals(actor.getId())) {
            throw new AuthorizationException("You do not own this team");
        }
        return team;
    }
}
