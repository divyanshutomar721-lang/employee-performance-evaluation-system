package com.epes.dao;

import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.model.Employee;
import com.epes.model.Manager;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link TeamDao} operations.
 */
class TeamDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Create team with members and verify employee belonging to multiple teams")
    void testTeamWithMembersAndMultiTeamEmployee() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        TeamDao teamDao = new TeamDaoImpl(connectionFactory);

        // Create manager
        Manager manager = new Manager();
        manager.setEmail("team.manager@epes.local");
        manager.setFullName("Team Lead Manager");
        User savedManager = userDao.create(connection, manager);

        // Create two employees
        Employee emp1 = new Employee();
        emp1.setEmail("emp.alpha@epes.local");
        emp1.setFullName("Alpha Employee");
        User savedEmp1 = userDao.create(connection, emp1);

        Employee emp2 = new Employee();
        emp2.setEmail("emp.beta@epes.local");
        emp2.setFullName("Beta Employee");
        User savedEmp2 = userDao.create(connection, emp2);

        // Create Team 1 with members
        Team team1 = new Team();
        team1.setName("Core Engineering");
        team1.setDescription("Backend Platform Team");
        team1.setManagerId(savedManager.getId());

        List<TeamMember> members1 = new ArrayList<>();
        members1.add(new TeamMember(null, savedEmp1.getId(), "Tech Lead", null));
        members1.add(new TeamMember(null, savedEmp2.getId(), "Developer", null));

        Team savedTeam1 = teamDao.createWithMembers(connection, team1, members1);
        assertNotNull(savedTeam1.getId());
        assertEquals(2, savedTeam1.getMembers().size());

        // Create Team 2 (emp1 is also a member in Team 2)
        Team team2 = new Team();
        team2.setName("Special Operations");
        team2.setDescription("R&D Task Force");
        team2.setManagerId(savedManager.getId());

        List<TeamMember> members2 = new ArrayList<>();
        members2.add(new TeamMember(null, savedEmp1.getId(), "Architect", null));

        Team savedTeam2 = teamDao.createWithMembers(connection, team2, members2);
        assertNotNull(savedTeam2.getId());

        // Verify findById loads members
        Optional<Team> loadedTeam1 = teamDao.findById(connection, savedTeam1.getId());
        assertTrue(loadedTeam1.isPresent());
        assertEquals(2, loadedTeam1.get().getMembers().size());

        // Verify emp1 is in both teams
        List<Team> emp1Teams = teamDao.findByEmployee(connection, savedEmp1.getId());
        assertEquals(2, emp1Teams.size());

        // Verify update member role
        teamDao.updateMemberRole(connection, savedTeam1.getId(), savedEmp2.getId(), "Senior Developer");
        List<TeamMember> updatedMembers = teamDao.findMembers(connection, savedTeam1.getId());
        TeamMember m2 = updatedMembers.stream()
                .filter(m -> m.getEmployeeId().equals(savedEmp2.getId()))
                .findFirst().orElseThrow();
        assertEquals("Senior Developer", m2.getRoleInTeam());

        // Verify remove member
        teamDao.removeMember(connection, savedTeam1.getId(), savedEmp2.getId());
        List<TeamMember> remaining = teamDao.findMembers(connection, savedTeam1.getId());
        assertEquals(1, remaining.size());
    }
}
