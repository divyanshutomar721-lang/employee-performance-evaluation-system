package com.epes.dao;

import com.epes.dao.impl.CriterionDaoImpl;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.model.Criterion;
import com.epes.model.Employee;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.Manager;
import com.epes.model.RocketStage;
import com.epes.model.Team;
import com.epes.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link EvaluationDao} operations.
 */
class EvaluationDaoTest extends BaseDaoTest {

    @Test
    @DisplayName("Save evaluation with scores, find by ID with scores, and query by employee")
    void testEvaluationSaveWithScoresAndQueries() throws Exception {
        UserDao userDao = new UserDaoImpl(connectionFactory);
        TeamDao teamDao = new TeamDaoImpl(connectionFactory);
        CriterionDao criterionDao = new CriterionDaoImpl(connectionFactory);
        EvaluationDao evaluationDao = new EvaluationDaoImpl(connectionFactory);

        // Setup users
        Manager manager = new Manager();
        manager.setEmail("eval.manager@epes.local");
        manager.setFullName("Eval Manager");
        User savedManager = userDao.create(connection, manager);

        Employee employee = new Employee();
        employee.setEmail("eval.employee@epes.local");
        employee.setFullName("Eval Employee");
        User savedEmployee = userDao.create(connection, employee);

        // Setup team
        Team team = new Team();
        team.setName("Eval Team");
        team.setManagerId(savedManager.getId());
        Team savedTeam = teamDao.create(connection, team);

        // Setup criterion
        Criterion criterion = new Criterion();
        criterion.setManagerId(savedManager.getId());
        criterion.setName("Problem Solving");
        criterion.setWeight(50);
        criterion.setActive(true);
        Criterion savedCriterion = criterionDao.create(connection, criterion);

        // Build Evaluation
        Evaluation eval = new Evaluation();
        eval.setEmployeeId(savedEmployee.getId());
        eval.setManagerId(savedManager.getId());
        eval.setTeamId(savedTeam.getId());
        eval.setPeriodLabel("Q3 2026");
        eval.setOverallScore(new BigDecimal("88.50"));
        eval.setOverallStage(RocketStage.CRUISE);
        eval.setSummaryComment("Consistently high performance throughout the quarter.");
        eval.setCreatedAt(LocalDateTime.now());

        List<EvaluationScore> scores = new ArrayList<>();
        EvaluationScore score1 = new EvaluationScore();
        score1.setCriterionId(savedCriterion.getId());
        score1.setCriterionName("Problem Solving");
        score1.setWeight(50);
        score1.setRating(5);
        score1.setComment("Excellent debugging abilities");
        scores.add(score1);

        EvaluationScore score2 = new EvaluationScore();
        score2.setCriterionId(null);
        score2.setCriterionName("Initiative");
        score2.setWeight(50);
        score2.setRating(4);
        score2.setComment("Proactive communication");
        scores.add(score2);

        Evaluation savedEval = evaluationDao.saveWithScores(connection, eval, scores);
        assertNotNull(savedEval.getId());
        assertEquals(2, savedEval.getScores().size());

        // Find by ID and check scores are loaded
        Optional<Evaluation> loaded = evaluationDao.findById(connection, savedEval.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Q3 2026", loaded.get().getPeriodLabel());
        assertEquals(RocketStage.CRUISE, loaded.get().getOverallStage());
        assertEquals(2, loaded.get().getScores().size());

        // Query by employee
        List<Evaluation> empEvals = evaluationDao.findByEmployee(connection, savedEmployee.getId());
        assertEquals(1, empEvals.size());

        // Query by manager
        List<Evaluation> mgrEvals = evaluationDao.findByManager(connection, savedManager.getId());
        assertEquals(1, mgrEvals.size());

        // Query by team
        List<Evaluation> teamEvals = evaluationDao.findByTeam(connection, savedTeam.getId());
        assertEquals(1, teamEvals.size());

        // Delete
        evaluationDao.delete(connection, savedEval.getId());
        Optional<Evaluation> deleted = evaluationDao.findById(connection, savedEval.getId());
        assertTrue(deleted.isEmpty());
    }
}
