package com.epes.service.impl;

import com.epes.dao.EvaluationDao;
import com.epes.dao.FeedbackDao;
import com.epes.dao.GoalDao;
import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.FeedbackDaoImpl;
import com.epes.dao.impl.GoalDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.AuthorizationException;
import com.epes.exception.DataAccessException;
import com.epes.exception.EntityNotFoundException;
import com.epes.exception.ValidationException;
import com.epes.model.Evaluation;
import com.epes.model.Feedback;
import com.epes.model.Goal;
import com.epes.model.GoalStatus;
import com.epes.model.RocketStage;
import com.epes.model.Role;
import com.epes.model.Team;
import com.epes.model.TeamMember;
import com.epes.model.User;
import com.epes.service.ReportService;
import com.epes.service.report.EmployeeReport;
import com.epes.service.report.EmployeeSummaryRow;
import com.epes.service.report.ManagerOverview;
import com.epes.service.report.ReportAggregator;
import com.epes.service.report.TeamReport;
import com.epes.service.report.Trend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of {@link ReportService} providing statistical aggregations,
 * trend detection, and multi-team rollups.
 */
public class ReportServiceImpl implements ReportService {

    private final UserDao userDao;
    private final TeamDao teamDao;
    private final EvaluationDao evaluationDao;
    private final GoalDao goalDao;
    private final FeedbackDao feedbackDao;

    /** Default constructor. */
    public ReportServiceImpl() throws DataAccessException {
        this(new UserDaoImpl(), new TeamDaoImpl(), new EvaluationDaoImpl(), new GoalDaoImpl(), new FeedbackDaoImpl());
    }

    /** Parameterized constructor. */
    public ReportServiceImpl(UserDao userDao, TeamDao teamDao, EvaluationDao evaluationDao, GoalDao goalDao, FeedbackDao feedbackDao) {
        this.userDao = userDao;
        this.teamDao = teamDao;
        this.evaluationDao = evaluationDao;
        this.goalDao = goalDao;
        this.feedbackDao = feedbackDao;
    }

    @Override
    public EmployeeReport buildEmployeeReport(User actor, Long employeeId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (employeeId == null) {
            throw new ValidationException("Employee ID cannot be null");
        }

        User employee = userDao.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with ID: " + employeeId));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isSelf = actor.getId().equals(employeeId);
        boolean isManagerOfEmployee = false;

        if (actor.getRole() == Role.MANAGER) {
            List<Team> empTeams = teamDao.findByEmployee(employeeId);
            isManagerOfEmployee = empTeams.stream().anyMatch(t -> t.getManagerId().equals(actor.getId()));
        }

        if (!isAdmin && !isSelf && !isManagerOfEmployee) {
            throw new AuthorizationException("You do not have permission to view reports for this employee");
        }

        // Evaluations: returned newest first, sort oldest first for chronological chart progression
        List<Evaluation> evals = new ArrayList<>(evaluationDao.findByEmployee(employeeId));
        evals.sort(Comparator.comparing(Evaluation::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        List<String> chartLabels = new ArrayList<>();
        List<Double> chartScores = new ArrayList<>();
        for (Evaluation e : evals) {
            chartLabels.add(e.getPeriodLabel());
            chartScores.add(e.getOverallScore() != null ? e.getOverallScore().doubleValue() : 0.0);
        }

        double averageScore = ReportAggregator.calculateAverageScore(evals);
        double latestScore = !evals.isEmpty() && evals.get(evals.size() - 1).getOverallScore() != null
                ? evals.get(evals.size() - 1).getOverallScore().doubleValue()
                : 0.0;
        RocketStage latestStage = !evals.isEmpty()
                ? evals.get(evals.size() - 1).getOverallStage()
                : RocketStage.LAUNCHPAD;
        Trend trend = ReportAggregator.calculateTrend(evals);

        List<Goal> goals = goalDao.findByEmployee(employeeId);
        Map<GoalStatus, Long> goalCounts = ReportAggregator.calculateGoalCounts(goals);

        List<Feedback> feedbackList = feedbackDao.findByEmployee(employeeId);
        double averageFeedbackRating = ReportAggregator.calculateAverageFeedbackRating(feedbackList);
        int feedbackCount = feedbackList.size();

        return new EmployeeReport(
                employee,
                evals,
                chartLabels,
                chartScores,
                averageScore,
                latestScore,
                latestStage,
                trend,
                goalCounts,
                averageFeedbackRating,
                feedbackCount
        );
    }

    @Override
    public TeamReport buildTeamReport(User actor, Long teamId)
            throws AuthorizationException, EntityNotFoundException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (teamId == null) {
            throw new ValidationException("Team ID cannot be null");
        }

        Team team = teamDao.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found with ID: " + teamId));

        boolean isAdmin = actor.getRole() == Role.ADMIN;
        boolean isOwner = team.getManagerId().equals(actor.getId());
        boolean isMember = team.getMembers().stream().anyMatch(m -> m.getEmployeeId().equals(actor.getId()));

        if (!isAdmin && !isOwner && !isMember) {
            throw new AuthorizationException("You do not have permission to view reports for this team");
        }

        List<EmployeeSummaryRow> rows = new ArrayList<>();
        List<RocketStage> memberLatestStages = new ArrayList<>();
        double totalMemberAvgSum = 0.0;
        int evaluatedMemberCount = 0;

        for (TeamMember member : team.getMembers()) {
            Long empId = member.getEmployeeId();
            User empUser = userDao.findById(empId).orElse(null);
            String empName = empUser != null ? empUser.getFullName() : "Employee #" + empId;

            List<Evaluation> empEvals = evaluationDao.findByEmployee(empId);
            double empAvg = ReportAggregator.calculateAverageScore(empEvals);
            RocketStage latestStage = !empEvals.isEmpty() ? empEvals.get(0).getOverallStage() : RocketStage.LAUNCHPAD;

            List<Goal> empGoals = goalDao.findByEmployee(empId);
            int goalCompletion = ReportAggregator.calculateGoalCompletionPercent(empGoals);

            rows.add(new EmployeeSummaryRow(empId, empName, empAvg, latestStage, goalCompletion));
            memberLatestStages.add(latestStage);

            if (!empEvals.isEmpty()) {
                totalMemberAvgSum += empAvg;
                evaluatedMemberCount++;
            }
        }

        // Sort rows by average score descending
        rows.sort(Comparator.comparingDouble(EmployeeSummaryRow::getAverageScore).reversed());

        Map<RocketStage, Long> stageDistribution = ReportAggregator.buildStageDistribution(memberLatestStages);
        double teamAverage = evaluatedMemberCount > 0
                ? BigDecimal.valueOf(totalMemberAvgSum / evaluatedMemberCount).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new TeamReport(team, rows, stageDistribution, teamAverage);
    }

    @Override
    public ManagerOverview buildManagerOverview(User actor)
            throws AuthorizationException, DataAccessException {
        if (actor == null) {
            throw new AuthorizationException("Authentication required");
        }
        if (actor.getRole() != Role.MANAGER && actor.getRole() != Role.ADMIN) {
            throw new AuthorizationException("Only managers and administrators can view management overviews");
        }

        List<Team> managedTeams = teamDao.findByManager(actor.getId());
        List<TeamReport> teamReports = new ArrayList<>();
        Set<Long> distinctEmployeeIds = new HashSet<>();
        double teamAvgSum = 0.0;
        int teamsWithEvals = 0;

        for (Team t : managedTeams) {
            try {
                TeamReport tr = buildTeamReport(actor, t.getId());
                teamReports.add(tr);
                for (EmployeeSummaryRow r : tr.getRows()) {
                    distinctEmployeeIds.add(r.getEmployeeId());
                }
                if (tr.getTeamAverageScore() > 0.0) {
                    teamAvgSum += tr.getTeamAverageScore();
                    teamsWithEvals++;
                }
            } catch (EntityNotFoundException ignored) {
            }
        }

        double overallAverage = teamsWithEvals > 0
                ? BigDecimal.valueOf(teamAvgSum / teamsWithEvals).setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        return new ManagerOverview(teamReports, distinctEmployeeIds.size(), overallAverage);
    }
}
