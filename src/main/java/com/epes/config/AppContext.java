package com.epes.config;

import com.epes.concurrent.GoalDeadlineScheduler;
import com.epes.concurrent.ReportJobService;
import com.epes.dao.CriterionDao;
import com.epes.dao.EvaluationDao;
import com.epes.dao.FeedbackDao;
import com.epes.dao.GoalDao;
import com.epes.dao.TeamDao;
import com.epes.dao.UserDao;
import com.epes.dao.impl.CriterionDaoImpl;
import com.epes.dao.impl.EvaluationDaoImpl;
import com.epes.dao.impl.FeedbackDaoImpl;
import com.epes.dao.impl.GoalDaoImpl;
import com.epes.dao.impl.TeamDaoImpl;
import com.epes.dao.impl.UserDaoImpl;
import com.epes.exception.DataAccessException;
import com.epes.service.AuthService;
import com.epes.service.CriterionService;
import com.epes.service.EvaluationService;
import com.epes.service.FeedbackService;
import com.epes.service.GoalService;
import com.epes.service.ReportService;
import com.epes.service.TeamService;
import com.epes.service.UserService;
import com.epes.service.impl.CriterionServiceImpl;
import com.epes.service.impl.EvaluationServiceImpl;
import com.epes.service.impl.FeedbackServiceImpl;
import com.epes.service.impl.GoalServiceImpl;
import com.epes.service.impl.ReportServiceImpl;
import com.epes.service.impl.TeamServiceImpl;
import com.epes.service.impl.TemporaryEmailAuthService;
import com.epes.service.impl.UserServiceImpl;
import com.epes.util.ConnectionFactory;
import com.epes.util.TransactionManager;

import java.util.logging.Logger;

/**
 * Central application registry and dependency wiring context.
 *
 * <p>Constructs and maintains single instances of all DAOs, services, and background concurrency
 * managers using plain constructor dependency injection without third-party frameworks.</p>
 */
public class AppContext {

    private static final Logger LOG = Logger.getLogger(AppContext.class.getName());

    // Infrastructure
    private final ConnectionFactory connectionFactory;
    private final TransactionManager transactionManager;

    // DAOs
    private final UserDao userDao;
    private final TeamDao teamDao;
    private final CriterionDao criterionDao;
    private final EvaluationDao evaluationDao;
    private final GoalDao goalDao;
    private final FeedbackDao feedbackDao;

    // Services
    private final AuthService authService;
    private final UserService userService;
    private final TeamService teamService;
    private final CriterionService criterionService;
    private final EvaluationService evaluationService;
    private final GoalService goalService;
    private final FeedbackService feedbackService;
    private final ReportService reportService;

    // Concurrency
    private final GoalDeadlineScheduler goalDeadlineScheduler;
    private final ReportJobService reportJobService;

    /**
     * Constructs a new AppContext with standard infrastructure wiring.
     *
     * @throws DataAccessException if low-level connection factory fails initialization
     */
    public AppContext() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Constructs an AppContext with an explicit {@link ConnectionFactory}.
     *
     * @param connectionFactory connection factory
     */
    public AppContext(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
        this.transactionManager = new TransactionManager(connectionFactory);

        // DAOs
        this.userDao = new UserDaoImpl(connectionFactory);
        this.teamDao = new TeamDaoImpl(connectionFactory);
        this.criterionDao = new CriterionDaoImpl(connectionFactory);
        this.evaluationDao = new EvaluationDaoImpl(connectionFactory);
        this.goalDao = new GoalDaoImpl(connectionFactory);
        this.feedbackDao = new FeedbackDaoImpl(connectionFactory);

        // Services
        this.authService = new TemporaryEmailAuthService(userDao);
        this.userService = new UserServiceImpl(userDao);
        this.teamService = new TeamServiceImpl(teamDao, userDao);
        this.criterionService = new CriterionServiceImpl(criterionDao);
        this.evaluationService = new EvaluationServiceImpl(evaluationDao, userDao, teamDao, criterionDao);
        this.goalService = new GoalServiceImpl(goalDao, userDao, teamDao, transactionManager);
        this.feedbackService = new FeedbackServiceImpl(feedbackDao, userDao, teamDao, evaluationDao);
        this.reportService = new ReportServiceImpl(userDao, teamDao, evaluationDao, goalDao, feedbackDao);

        // Concurrency
        this.goalDeadlineScheduler = new GoalDeadlineScheduler(goalService);
        this.reportJobService = new ReportJobService();
    }

    /**
     * Starts all background tasks and scheduled daemons.
     */
    public void start() {
        LOG.info("Starting AppContext background services...");
        goalDeadlineScheduler.start();
        LOG.info("AppContext started successfully.");
    }

    /**
     * Gracefully stops all background threads and executors.
     */
    public void shutdown() {
        LOG.info("Shutting down AppContext...");
        goalDeadlineScheduler.stop();
        reportJobService.shutdown();
        LOG.info("AppContext shut down completed.");
    }

    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public TeamDao getTeamDao() {
        return teamDao;
    }

    public CriterionDao getCriterionDao() {
        return criterionDao;
    }

    public EvaluationDao getEvaluationDao() {
        return evaluationDao;
    }

    public GoalDao getGoalDao() {
        return goalDao;
    }

    public FeedbackDao getFeedbackDao() {
        return feedbackDao;
    }

    public AuthService getAuthService() {
        return authService;
    }

    public UserService getUserService() {
        return userService;
    }

    public TeamService getTeamService() {
        return teamService;
    }

    public CriterionService getCriterionService() {
        return criterionService;
    }

    public EvaluationService getEvaluationService() {
        return evaluationService;
    }

    public GoalService getGoalService() {
        return goalService;
    }

    public FeedbackService getFeedbackService() {
        return feedbackService;
    }

    public ReportService getReportService() {
        return reportService;
    }

    public GoalDeadlineScheduler getGoalDeadlineScheduler() {
        return goalDeadlineScheduler;
    }

    public ReportJobService getReportJobService() {
        return reportJobService;
    }
}
