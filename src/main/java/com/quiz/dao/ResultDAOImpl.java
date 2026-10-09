package com.quiz.dao;

import com.quiz.dto.LeaderboardEntryDto;
import com.quiz.model.AttemptStatus;
import com.quiz.model.Quiz;
import com.quiz.model.QuizAttempt;
import com.quiz.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Direct JDBC Implementation of ResultDAO using PreparedStatements.
 */
@Repository("resultJdbcDAO")
public class ResultDAOImpl implements ResultDAO {

    private final DataSource dataSource;

    public ResultDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<QuizAttempt> findById(Long id) {
        String sql = "SELECT qa.id, qa.user_id, qa.quiz_id, qa.score, qa.total_marks, qa.total_questions, " +
                "qa.correct_answers, qa.started_at, qa.completed_at, qa.time_taken_seconds, qa.status, qa.passed " +
                "FROM quiz_attempts qa WHERE qa.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding attempt by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<QuizAttempt> findByUserId(Long userId) {
        String sql = "SELECT qa.id, qa.user_id, qa.quiz_id, qa.score, qa.total_marks, qa.total_questions, " +
                "qa.correct_answers, qa.started_at, qa.completed_at, qa.time_taken_seconds, qa.status, qa.passed " +
                "FROM quiz_attempts qa WHERE qa.user_id = ? ORDER BY qa.started_at DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding attempts by user ID: " + userId, e);
        }
        return list;
    }

    @Override
    public List<QuizAttempt> findByQuizId(Long quizId) {
        String sql = "SELECT qa.id, qa.user_id, qa.quiz_id, qa.score, qa.total_marks, qa.total_questions, " +
                "qa.correct_answers, qa.started_at, qa.completed_at, qa.time_taken_seconds, qa.status, qa.passed " +
                "FROM quiz_attempts qa WHERE qa.quiz_id = ? ORDER BY qa.started_at DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAttempt(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding attempts by quiz ID: " + quizId, e);
        }
        return list;
    }

    @Override
    public List<QuizAttempt> findAll() {
        String sql = "SELECT qa.id, qa.user_id, qa.quiz_id, qa.score, qa.total_marks, qa.total_questions, " +
                "qa.correct_answers, qa.started_at, qa.completed_at, qa.time_taken_seconds, qa.status, qa.passed " +
                "FROM quiz_attempts qa ORDER BY qa.started_at DESC";
        List<QuizAttempt> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToAttempt(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding all attempts", e);
        }
        return list;
    }

    @Override
    public List<LeaderboardEntryDto> getGlobalLeaderboard(int limit) {
        String sql = "SELECT u.id AS user_id, u.username, u.full_name, " +
                "SUM(qa.score) AS total_score, SUM(qa.total_marks) AS max_marks, " +
                "COUNT(qa.id) AS attempts_count, " +
                "MAX(qa.completed_at) AS last_completed " +
                "FROM quiz_attempts qa " +
                "JOIN users u ON qa.user_id = u.id " +
                "WHERE qa.status = 'COMPLETED' " +
                "GROUP BY u.id, u.username, u.full_name " +
                "ORDER BY total_score DESC, last_completed ASC LIMIT ?";

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    int score = rs.getInt("total_score");
                    int totalMarks = rs.getInt("max_marks");
                    double pct = totalMarks > 0 ? ((double) score / totalMarks) * 100.0 : 0.0;
                    Timestamp ts = rs.getTimestamp("last_completed");

                    LeaderboardEntryDto entry = new LeaderboardEntryDto(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            null,
                            "Global Platform",
                            score,
                            totalMarks,
                            Math.round(pct * 10.0) / 10.0,
                            0L,
                            rank++,
                            ts != null ? ts.toLocalDateTime() : null
                    );
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error calculating global leaderboard", e);
        }
        return entries;
    }

    @Override
    public List<LeaderboardEntryDto> getQuizLeaderboard(Long quizId, int limit) {
        String sql = "SELECT u.id AS user_id, u.username, u.full_name, q.id AS quiz_id, q.title AS quiz_title, " +
                "qa.score, qa.total_marks, qa.time_taken_seconds, qa.completed_at " +
                "FROM quiz_attempts qa " +
                "JOIN users u ON qa.user_id = u.id " +
                "JOIN quizzes q ON qa.quiz_id = q.id " +
                "WHERE qa.quiz_id = ? AND qa.status = 'COMPLETED' " +
                "ORDER BY qa.score DESC, qa.time_taken_seconds ASC LIMIT ?";

        List<LeaderboardEntryDto> entries = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, quizId);
            ps.setInt(2, limit > 0 ? limit : 10);
            try (ResultSet rs = ps.executeQuery()) {
                int rank = 1;
                while (rs.next()) {
                    int score = rs.getInt("score");
                    int totalMarks = rs.getInt("total_marks");
                    double pct = totalMarks > 0 ? ((double) score / totalMarks) * 100.0 : 0.0;
                    Timestamp ts = rs.getTimestamp("completed_at");

                    LeaderboardEntryDto entry = new LeaderboardEntryDto(
                            rs.getLong("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getLong("quiz_id"),
                            rs.getString("quiz_title"),
                            score,
                            totalMarks,
                            Math.round(pct * 10.0) / 10.0,
                            rs.getLong("time_taken_seconds"),
                            rank++,
                            ts != null ? ts.toLocalDateTime() : null
                    );
                    entries.add(entry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error calculating quiz leaderboard", e);
        }
        return entries;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM quiz_attempts";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting quiz attempts", e);
        }
        return 0;
    }

    private QuizAttempt mapRowToAttempt(ResultSet rs) throws SQLException {
        QuizAttempt qa = new QuizAttempt();
        qa.setId(rs.getLong("id"));

        User u = new User();
        u.setId(rs.getLong("user_id"));
        qa.setUser(u);

        Quiz q = new Quiz();
        q.setId(rs.getLong("quiz_id"));
        qa.setQuiz(q);

        qa.setScore(rs.getInt("score"));
        qa.setTotalMarks(rs.getInt("total_marks"));
        qa.setTotalQuestions(rs.getInt("total_questions"));
        qa.setCorrectAnswers(rs.getInt("correct_answers"));

        Timestamp st = rs.getTimestamp("started_at");
        if (st != null) {
            qa.setStartedAt(st.toLocalDateTime());
        }
        Timestamp ct = rs.getTimestamp("completed_at");
        if (ct != null) {
            qa.setCompletedAt(ct.toLocalDateTime());
        }

        qa.setTimeTakenSeconds(rs.getLong("time_taken_seconds"));
        qa.setStatus(AttemptStatus.valueOf(rs.getString("status")));
        qa.setPassed(rs.getBoolean("passed"));
        return qa;
    }
}
