package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;

import java.util.List;
import java.util.Map;

/**
 * Service interface for team and membership management.
 */
public interface TeamService {

    /**
     * Creates a new team with initial members. Restricted to managers.
     *
     * @param actor              authenticated manager creating the team
     * @param name               team name
     * @param description        team description
     * @param employeeIds        list of employee user IDs to add
     * @param roleByEmployeeId   map of employee ID to their role in the team (defaults to "Member")
     * @return created team with members
     * @throws AuthorizationException if actor is not a manager
     * @throws ValidationException    if inputs are invalid or name is duplicated
     * @throws BusinessRuleException  if a member is not an active employee
     * @throws DataAccessException   if a database error occurs
     */
    Team createTeam(User actor, String name, String description,
                    List<Long> employeeIds, Map<Long, String> roleByEmployeeId)
            throws AuthorizationException, ValidationException, BusinessRuleException, DataAccessException;

    /**
     * Updates an existing team's name and description. Only owning manager.
     */
    Team updateTeam(User actor, Long teamId, String name, String description)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException;

    /**
     * Deletes a team. Only owning manager.
     */
    void deleteTeam(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Adds an employee to a team. Only owning manager.
     */
    TeamMember addMember(User actor, Long teamId, Long employeeId, String roleInTeam)
            throws AuthorizationException, EntityNotFoundException, BusinessRuleException, ValidationException, DataAccessException;

    /**
     * Updates an employee's role in a team. Only owning manager.
     */
    void updateMemberRole(User actor, Long teamId, Long employeeId, String roleInTeam)
            throws AuthorizationException, EntityNotFoundException, ValidationException, DataAccessException;

    /**
     * Removes an employee from a team. Only owning manager.
     */
    void removeMember(User actor, Long teamId, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Retrieves a team with members. Accessible by owning manager, member employee, or admin.
     */
    Team getTeam(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException;

    /**
     * Lists teams relevant to the actor (managed teams for managers, member teams for employees).
     */
    List<Team> listMyTeams(User actor) throws AuthorizationException, DataAccessException;

    /**
     * Lists active employees eligible for assignment to teams.
     * Restricted to managers.
     *
     * @param actor authenticated manager
     * @return list of active employee summaries sorted by full name case-insensitively
     * @throws AuthorizationException if actor is not a manager
     * @throws DataAccessException    if a database error occurs
     */
    List<UserSummary> listAssignableEmployees(User actor) throws AuthorizationException, DataAccessException;

    /**
     * Lists distinct active employees who are members of any team managed by the actor.
     * Restricted to managers.
     *
     * @param actor authenticated manager
     * @return list of distinct active employee summaries sorted by full name case-insensitively
     * @throws AuthorizationException if actor is not a manager
     * @throws DataAccessException    if a database error occurs
     */
    List<UserSummary> listMyEmployees(User actor) throws AuthorizationException, DataAccessException;
}
