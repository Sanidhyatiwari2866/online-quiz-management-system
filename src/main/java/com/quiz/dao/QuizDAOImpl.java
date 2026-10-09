package com.quiz.dao;

import com.quiz.model.Category;
import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import com.quiz.model.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Direct JDBC Implementation of QuizDAO using PreparedStatements.
 */
@Repository("quizJdbcDAO")
public class QuizDAOImpl implements QuizDAO {

    private final DataSource dataSource;

    public QuizDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        String sql = "SELECT q.id, q.title, q.description, q.time_limit_minutes, q.total_marks, q.passing_score, " +
                "q.randomize_questions, q.status, q.rejection_reason, q.category_id, q.created_by_user_id, " +
                "q.created_at, q.updated_at FROM quizzes q WHERE q.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding quiz by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Quiz> findAll() {
        String sql = "SELECT id, title, description, time_limit_minutes, total_marks, passing_score, " +
                "randomize_questions, status, rejection_reason, category_id, created_by_user_id, " +
                "created_at, updated_at FROM quizzes ORDER BY id DESC";
        List<Quiz> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToQuiz(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error listing all quizzes", e);
        }
        return list;
    }

    @Override
    public List<Quiz> findByStatus(QuizStatus status) {
        String sql = "SELECT id, title, description, time_limit_minutes, total_marks, passing_score, " +
                "randomize_questions, status, rejection_reason, category_id, created_by_user_id, " +
                "created_at, updated_at FROM quizzes WHERE status = ? ORDER BY id DESC";
        List<Quiz> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToQuiz(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error listing quizzes by status: " + status, e);
        }
        return list;
    }

    @Override
    public Quiz save(Quiz quiz) {
        if (quiz.getId() == null) {
            String sql = "INSERT INTO quizzes (title, description, time_limit_minutes, total_marks, passing_score, " +
                    "randomize_questions, status, rejection_reason, category_id, created_by_user_id, created_at, updated_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                LocalDateTime now = LocalDateTime.now();
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getDescription());
                ps.setInt(3, quiz.getTimeLimitMinutes());
                ps.setInt(4, quiz.getTotalMarks() != null ? quiz.getTotalMarks() : 0);
                ps.setInt(5, quiz.getPassingScore());
                ps.setBoolean(6, quiz.getRandomizeQuestions() != null ? quiz.getRandomizeQuestions() : false);
                ps.setString(7, quiz.getStatus() != null ? quiz.getStatus().name() : QuizStatus.APPROVED.name());
                ps.setString(8, quiz.getRejectionReason());
                ps.setLong(9, quiz.getCategory() != null ? quiz.getCategory().getId() : 1L);
                if (quiz.getCreator() != null && quiz.getCreator().getId() != null) {
                    ps.setLong(10, quiz.getCreator().getId());
                } else {
                    ps.setNull(10, Types.BIGINT);
                }
                ps.setTimestamp(11, Timestamp.valueOf(now));
                ps.setTimestamp(12, Timestamp.valueOf(now));

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        quiz.setId(keys.getLong(1));
                    }
                }
                quiz.setCreatedAt(now);
                quiz.setUpdatedAt(now);
                return quiz;
            } catch (SQLException e) {
                throw new RuntimeException("Database error creating quiz", e);
            }
        } else {
            String sql = "UPDATE quizzes SET title = ?, description = ?, time_limit_minutes = ?, total_marks = ?, " +
                    "passing_score = ?, randomize_questions = ?, status = ?, rejection_reason = ?, updated_at = ? WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                LocalDateTime now = LocalDateTime.now();
                ps.setString(1, quiz.getTitle());
                ps.setString(2, quiz.getDescription());
                ps.setInt(3, quiz.getTimeLimitMinutes());
                ps.setInt(4, quiz.getTotalMarks() != null ? quiz.getTotalMarks() : 0);
                ps.setInt(5, quiz.getPassingScore());
                ps.setBoolean(6, quiz.getRandomizeQuestions() != null ? quiz.getRandomizeQuestions() : false);
                ps.setString(7, quiz.getStatus().name());
                ps.setString(8, quiz.getRejectionReason());
                ps.setTimestamp(9, Timestamp.valueOf(now));
                ps.setLong(10, quiz.getId());

                ps.executeUpdate();
                quiz.setUpdatedAt(now);
                return quiz;
            } catch (SQLException e) {
                throw new RuntimeException("Database error updating quiz: " + quiz.getId(), e);
            }
        }
    }

    @Override
    public boolean updateStatus(Long quizId, QuizStatus status, String rejectionReason) {
        String sql = "UPDATE quizzes SET status = ?, rejection_reason = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setString(2, rejectionReason);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(4, quizId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating quiz approval status: " + quizId, e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM quizzes WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting quiz: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM quizzes";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting quizzes", e);
        }
        return 0;
    }

    private Quiz mapRowToQuiz(ResultSet rs) throws SQLException {
        Quiz q = new Quiz();
        q.setId(rs.getLong("id"));
        q.setTitle(rs.getString("title"));
        q.setDescription(rs.getString("description"));
        q.setTimeLimitMinutes(rs.getInt("time_limit_minutes"));
        q.setTotalMarks(rs.getInt("total_marks"));
        q.setPassingScore(rs.getInt("passing_score"));
        q.setRandomizeQuestions(rs.getBoolean("randomize_questions"));
        String st = rs.getString("status");
        q.setStatus(st != null ? QuizStatus.valueOf(st) : QuizStatus.APPROVED);
        q.setRejectionReason(rs.getString("rejection_reason"));

        Category c = new Category();
        c.setId(rs.getLong("category_id"));
        q.setCategory(c);

        long creatorId = rs.getLong("created_by_user_id");
        if (!rs.wasNull()) {
            User u = new User();
            u.setId(creatorId);
            q.setCreator(u);
        }

        Timestamp cat = rs.getTimestamp("created_at");
        if (cat != null) {
            q.setCreatedAt(cat.toLocalDateTime());
        }
        Timestamp uat = rs.getTimestamp("updated_at");
        if (uat != null) {
            q.setUpdatedAt(uat.toLocalDateTime());
        }
        return q;
    }
}
