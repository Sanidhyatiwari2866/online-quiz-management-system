package com.quiz.dao;

import com.quiz.model.Feedback;
import com.quiz.model.Quiz;
import com.quiz.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Direct JDBC Implementation of FeedbackDAO using PreparedStatements.
 */
@Repository("feedbackJdbcDAO")
public class FeedbackDAOImpl implements FeedbackDAO {

    private final DataSource dataSource;

    public FeedbackDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Feedback save(Feedback feedback) {
        String sql = "INSERT INTO feedback (user_id, quiz_id, rating, comment, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            ps.setLong(1, feedback.getUser().getId());
            ps.setLong(2, feedback.getQuiz().getId());
            ps.setInt(3, feedback.getRating());
            ps.setString(4, feedback.getComment());
            ps.setTimestamp(5, Timestamp.valueOf(now));
            ps.setTimestamp(6, Timestamp.valueOf(now));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    feedback.setId(keys.getLong(1));
                }
            }
            feedback.setCreatedAt(now);
            feedback.setUpdatedAt(now);
            return feedback;
        } catch (SQLException e) {
            throw new RuntimeException("Database error saving feedback", e);
        }
    }

    @Override
    public List<Feedback> findByQuizId(Long quizId) {
        String sql = "SELECT f.id, f.user_id, u.username, u.full_name, f.quiz_id, f.rating, f.comment, f.created_at, f.updated_at " +
                "FROM feedback f JOIN users u ON f.user_id = u.id WHERE f.quiz_id = ? ORDER BY f.created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding feedback for quiz: " + quizId, e);
        }
        return list;
    }

    @Override
    public List<Feedback> findByUserId(Long userId) {
        String sql = "SELECT f.id, f.user_id, u.username, u.full_name, f.quiz_id, f.rating, f.comment, f.created_at, f.updated_at " +
                "FROM feedback f JOIN users u ON f.user_id = u.id WHERE f.user_id = ? ORDER BY f.created_at DESC";
        List<Feedback> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToFeedback(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding feedback for user: " + userId, e);
        }
        return list;
    }

    @Override
    public double getAverageRatingForQuiz(Long quizId) {
        String sql = "SELECT AVG(rating) FROM feedback WHERE quiz_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Math.round(rs.getDouble(1) * 10.0) / 10.0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error calculating average rating for quiz: " + quizId, e);
        }
        return 0.0;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM feedback";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting feedback", e);
        }
        return 0;
    }

    private Feedback mapRowToFeedback(ResultSet rs) throws SQLException {
        Feedback f = new Feedback();
        f.setId(rs.getLong("id"));

        User u = new User();
        u.setId(rs.getLong("user_id"));
        u.setUsername(rs.getString("username"));
        u.setFullName(rs.getString("full_name"));
        f.setUser(u);

        Quiz q = new Quiz();
        q.setId(rs.getLong("quiz_id"));
        f.setQuiz(q);

        f.setRating(rs.getInt("rating"));
        f.setComment(rs.getString("comment"));

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) {
            f.setCreatedAt(cat.toLocalDateTime());
        }
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) {
            f.setUpdatedAt(uat.toLocalDateTime());
        }
        return f;
    }
}
