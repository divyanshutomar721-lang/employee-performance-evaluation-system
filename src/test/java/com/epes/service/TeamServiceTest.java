package com.epes.service;

import com.epes.exception.AuthorizationException;
import com.epes.exception.BusinessRuleException;
import com.epes.exception.ValidationException;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;
import com.epes.model.UserFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TeamService}.
 */
class TeamServiceTest extends BaseServiceTest {

    @Test
    @DisplayName("Create team with members, verify multi-team membership, and manage members")
    void testTeamLifecycleAndMultiTeamEmployee() throws Exception {
        User manager = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Team Manager", Role.MANAGER, true, null));
        User emp1 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp One", Role.EMPLOYEE, true, null));
        User emp2 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Emp Two", Role.EMPLOYEE, true, null));

        String teamName1 = randomTeamName();
        Team team1 = appContext.getTeamService().createTeam(
                manager,
                teamName1,
                "Primary Engineering",
                List.of(emp1.getId(), emp2.getId()),
                Map.of(emp1.getId(), "Lead Developer", emp2.getId(), "Tester")
        );
        assertNotNull(team1.getId());
        assertEquals(2, team1.getMembers().size());

        // Create second team with emp1
        String teamName2 = randomTeamName();
        Team team2 = appContext.getTeamService().createTeam(
                manager,
                teamName2,
                "Secondary R&D",
                List.of(emp1.getId()),
                Map.of(emp1.getId(), "Specialist")
        );
        assertNotNull(team2.getId());

        // Verify emp1 belongs to both teams
        List<Team> emp1Teams = appContext.getTeamService().listMyTeams(emp1);
        assertEquals(2, emp1Teams.size());

        // Update member role
        appContext.getTeamService().updateMemberRole(manager, team1.getId(), emp2.getId(), "Senior QA Engineer");
        Team loaded = appContext.getTeamService().getTeam(manager, team1.getId());
        TeamMember m = loaded.getMembers().stream()
                .filter(x -> x.getEmployeeId().equals(emp2.getId()))
                .findFirst().orElseThrow();
        assertEquals("Senior QA Engineer", m.getRoleInTeam());

        // Remove member
        appContext.getTeamService().removeMember(manager, team1.getId(), emp2.getId());
        Team afterRemove = appContext.getTeamService().getTeam(manager, team1.getId());
        assertEquals(1, afterRemove.getMembers().size());
    }

    @Test
    @DisplayName("Non-manager or non-owner actions are refused")
    void testAuthorizationProtections() throws Exception {
        User manager1 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager One", Role.MANAGER, true, null));
        User manager2 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager Two", Role.MANAGER, true, null));
        User employee = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Employee User", Role.EMPLOYEE, true, null));

        // Employee cannot create team
        assertThrows(AuthorizationException.class, () ->
                appContext.getTeamService().createTeam(employee, randomTeamName(), "Desc", List.of(), Map.of()));

        // Manager 1 creates team
        Team t = appContext.getTeamService().createTeam(manager1, randomTeamName(), "Desc", List.of(), Map.of());

        // Manager 2 cannot update or delete Manager 1's team
        assertThrows(AuthorizationException.class, () ->
                appContext.getTeamService().updateTeam(manager2, t.getId(), "Hacked Name", "Desc"));

        assertThrows(AuthorizationException.class, () ->
                appContext.getTeamService().deleteTeam(manager2, t.getId()));
    }

    @Test
    @DisplayName("Adding a non-EMPLOYEE (e.g. Manager or Admin) as a team member throws BusinessRuleException")
    void testNonEmployeeMemberRejected() throws Exception {
        User manager1 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Manager Lead", Role.MANAGER, true, null));
        User manager2 = appContext.getUserDao().create(UserFactory.create(
                null, randomEmail(), "Other Manager", Role.MANAGER, true, null));

        assertThrows(BusinessRuleException.class, () ->
                appContext.getTeamService().createTeam(manager1, randomTeamName(), "Desc",
                        List.of(manager2.getId()), Map.of()));
    }
}
