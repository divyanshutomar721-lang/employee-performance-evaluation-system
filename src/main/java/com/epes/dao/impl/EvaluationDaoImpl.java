package com.epes.dao.impl;

import com.epes.dao.EvaluationDao;
import com.epes.exception.DataAccessException;
import com.epes.exception.DuplicateEntityException;
import com.epes.exception.EntityNotFoundException;
import com.epes.model.Evaluation;
import com.epes.model.EvaluationScore;
import com.epes.model.RocketStage;
import com.epes.util.ConnectionFactory;
import com.epes.util.TransactionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of {@link EvaluationDao}.
 */
public class EvaluationDaoImpl implements EvaluationDao {

    private static final String SQL_STATE_UNIQUE_VIOLATION = "23505";
    private final ConnectionFactory connectionFactory;

    /**
     * Default constructor.
     *
     * @throws DataAccessException if connection factory initialization fails
     */
    public EvaluationDaoImpl() throws DataAccessException {
        this(new ConnectionFactory());
    }

    /**
     * Parameterized constructor.
     *
     * @param connectionFactory connection factory
     */
    public EvaluationDaoImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Evaluation saveWithScores(Evaluation evaluation, List<EvaluationScore> scores) throws DataAccessException {
        TransactionManager tm = new TransactionManager(connectionFactory);
        return tm.executeInTransaction(conn -> saveWithScores(conn, evaluation, scores));
    }

    @Override
    public Evaluation saveWithScores(Connection conn, Evaluation evaluation, List<EvaluationScore> scores) throws DataAccessException {
        String sqlEval = "INSERT INTO evaluations (employee_id, manager_id, team_id, period_label, overall_score, overall_stage, summary_comment, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        LocalDateTime now = evaluation.getCreatedAt() != null ? evaluation.getCreatedAt() : LocalDateTime.now();

        try (PreparedStatement ps = conn.prepareStatement(sqlEval)) {
            ps.setLong(1, evaluation.getEmployeeId());
            ps.setLong(2, evaluation.getManagerId());
            if (evaluation.getTeamId() != null) {
                ps.setLong(3, evaluation.getTeamId());
            } else {
                ps.setNull(3, Types.BIGINT);
            }
            ps.setString(4, evaluation.getPeriodLabel());
            ps.setBigDecimal(5, evaluation.getOverallScore());
            ps.setString(6, evaluation.getOverallStage().name());
            ps.setString(7, evaluation.getSummaryComment());
            ps.setTimestamp(8, Timestamp.valueOf(now));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    evaluation.setId(rs.getLong("id"));
                    evaluation.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                } else {
                    throw new DataAccessException("Saving evaluation failed, no ID returned.");
                }
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to save evaluation header", e);
        }

        List<EvaluationScore> savedScores = new ArrayList<>();
        if (scores != null && !scores.isEmpty()) {
            String sqlScore = "INSERT INTO evaluation_scores (evaluation_id, criterion_id, criterion_name, weight, rating, comment) " +
                    "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

            try (PreparedStatement psScore = conn.prepareStatement(sqlScore)) {
                for (EvaluationScore score : scores) {
                    psScore.setLong(1, evaluation.getId());
                    if (score.getCriterionId() != null) {
                        psScore.setLong(2, score.getCriterionId());
                    } else {
                        psScore.setNull(2, Types.BIGINT);
                    }
                    psScore.setString(3, score.getCriterionName());
                    psScore.setInt(4, score.getWeight());
                    psScore.setInt(5, score.getRating());
                    psScore.setString(6, score.getComment());

                    try (ResultSet rs = psScore.executeQuery()) {
                        if (rs.next()) {
                            score.setId(rs.getLong("id"));
                            score.setEvaluationId(evaluation.getId());
                            savedScores.add(score);
                        }
                    }
                }
            } catch (SQLException e) {
                throw handleSqlException("Failed to save evaluation score records", e);
            }
        }

        evaluation.setScores(savedScores);
        return evaluation;
    }

    @Override
    public Optional<Evaluation> findById(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findById(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findById evaluation", e);
        }
    }

    @Override
    public Optional<Evaluation> findById(Connection conn, Long id) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, team_id, period_label, overall_score, overall_stage, summary_comment, created_at " +
                "FROM evaluations WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Evaluation evaluation = mapRow(rs);
                    evaluation.setScores(findScoresByEvaluationId(conn, id));
                    return Optional.of(evaluation);
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find evaluation by ID: " + id, e);
        }
    }

    @Override
    public List<Evaluation> findByEmployee(Long employeeId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByEmployee(conn, employeeId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByEmployee evaluations", e);
        }
    }

    @Override
    public List<Evaluation> findByEmployee(Connection conn, Long employeeId) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, team_id, period_label, overall_score, overall_stage, summary_comment, created_at " +
                "FROM evaluations WHERE employee_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, employeeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Evaluation> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find evaluations for employee ID: " + employeeId, e);
        }
    }

    @Override
    public List<Evaluation> findByManager(Long managerId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByManager(conn, managerId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByManager evaluations", e);
        }
    }

    @Override
    public List<Evaluation> findByManager(Connection conn, Long managerId) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, team_id, period_label, overall_score, overall_stage, summary_comment, created_at " +
                "FROM evaluations WHERE manager_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Evaluation> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find evaluations for manager ID: " + managerId, e);
        }
    }

    @Override
    public List<Evaluation> findByTeam(Long teamId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findByTeam(conn, teamId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findByTeam evaluations", e);
        }
    }

    @Override
    public List<Evaluation> findByTeam(Connection conn, Long teamId) throws DataAccessException {
        String sql = "SELECT id, employee_id, manager_id, team_id, period_label, overall_score, overall_stage, summary_comment, created_at " +
                "FROM evaluations WHERE team_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, teamId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Evaluation> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find evaluations for team ID: " + teamId, e);
        }
    }

    @Override
    public void delete(Long id) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            delete(conn, id);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for delete evaluation", e);
        }
    }

    @Override
    public void delete(Connection conn, Long id) throws DataAccessException {
        String sql = "DELETE FROM evaluations WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new EntityNotFoundException("Evaluation not found for delete with ID: " + id);
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to delete evaluation ID: " + id, e);
        }
    }

    @Override
    public List<EvaluationScore> findScoresByEvaluationId(Long evaluationId) throws DataAccessException {
        try (Connection conn = connectionFactory.getConnection()) {
            return findScoresByEvaluationId(conn, evaluationId);
        } catch (SQLException e) {
            throw handleSqlException("Failed to open connection for findScoresByEvaluationId", e);
        }
    }

    @Override
    public List<EvaluationScore> findScoresByEvaluationId(Connection conn, Long evaluationId) throws DataAccessException {
        String sql = "SELECT id, evaluation_id, criterion_id, criterion_name, weight, rating, comment " +
                "FROM evaluation_scores WHERE evaluation_id = ? ORDER BY id ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, evaluationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<EvaluationScore> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(mapScoreRow(rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw handleSqlException("Failed to find scores for evaluation ID: " + evaluationId, e);
        }
    }

    private Evaluation mapRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long employeeId = rs.getLong("employee_id");
        Long managerId = rs.getLong("manager_id");
        Long teamId = rs.getObject("team_id", Long.class);
        String periodLabel = rs.getString("period_label");
        BigDecimal overallScore = rs.getBigDecimal("overall_score");
        RocketStage overallStage = RocketStage.valueOf(rs.getString("overall_stage"));
        String summaryComment = rs.getString("summary_comment");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : null;

        return new Evaluation(id, employeeId, managerId, teamId, periodLabel, overallScore, overallStage, summaryComment, createdAt);
    }

    private EvaluationScore mapScoreRow(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long evalId = rs.getLong("evaluation_id");
        Long criterionId = rs.getObject("criterion_id", Long.class);
        String criterionName = rs.getString("criterion_name");
        int weight = rs.getInt("weight");
        int rating = rs.getInt("rating");
        String comment = rs.getString("comment");

        return new EvaluationScore(id, evalId, criterionId, criterionName, weight, rating, comment);
    }

    private DataAccessException handleSqlException(String message, SQLException e) {
        if (SQL_STATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
            return new DuplicateEntityException(message + " (Duplicate entity / unique constraint violation)", e);
        }
        return new DataAccessException(message + ": " + e.getMessage(), e);
    }
}
