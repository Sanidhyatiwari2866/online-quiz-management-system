package com.quiz.dao;

import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.QuestionType;
import com.quiz.model.Quiz;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Direct JDBC Implementation of QuestionDAO using PreparedStatements and batch operations.
 */
@Repository("questionJdbcDAO")
public class QuestionDAOImpl implements QuestionDAO {

    private final DataSource dataSource;

    public QuestionDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Question> findById(Long id) {
        String sql = "SELECT id, question_text, question_type, marks, explanation, quiz_id FROM questions WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Question q = mapRowToQuestion(rs);
                    q.setOptions(findOptionsByQuestionId(conn, id));
                    return Optional.of(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding question by ID: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Question> findByQuizId(Long quizId) {
        String sql = "SELECT id, question_text, question_type, marks, explanation, quiz_id FROM questions WHERE quiz_id = ? ORDER BY id ASC";
        List<Question> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRowToQuestion(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding questions by quiz ID: " + quizId, e);
        }
        return list;
    }

    @Override
    public Question save(Question question) {
        if (question.getId() == null) {
            String sql = "INSERT INTO questions (question_text, question_type, marks, explanation, quiz_id) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, question.getQuestionText());
                ps.setString(2, question.getQuestionType().name());
                ps.setInt(3, question.getMarks() != null ? question.getMarks() : 1);
                ps.setString(4, question.getExplanation());
                ps.setLong(5, question.getQuiz() != null ? question.getQuiz().getId() : 1L);

                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        question.setId(keys.getLong(1));
                    }
                }

                // Save options
                if (question.getOptions() != null && !question.getOptions().isEmpty()) {
                    String optSql = "INSERT INTO options (option_text, is_correct, question_id) VALUES (?, ?, ?)";
                    try (PreparedStatement optPs = conn.prepareStatement(optSql, Statement.RETURN_GENERATED_KEYS)) {
                        for (Option opt : question.getOptions()) {
                            optPs.setString(1, opt.getOptionText());
                            optPs.setBoolean(2, Boolean.TRUE.equals(opt.getIsCorrect()));
                            optPs.setLong(3, question.getId());
                            optPs.executeUpdate();
                            try (ResultSet optKeys = optPs.getGeneratedKeys()) {
                                if (optKeys.next()) {
                                    opt.setId(optKeys.getLong(1));
                                }
                            }
                        }
                    }
                }
                return question;
            } catch (SQLException e) {
                throw new RuntimeException("Database error saving question", e);
            }
        } else {
            String sql = "UPDATE questions SET question_text = ?, question_type = ?, marks = ?, explanation = ? WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, question.getQuestionText());
                ps.setString(2, question.getQuestionType().name());
                ps.setInt(3, question.getMarks() != null ? question.getMarks() : 1);
                ps.setString(4, question.getExplanation());
                ps.setLong(5, question.getId());

                ps.executeUpdate();
                return question;
            } catch (SQLException e) {
                throw new RuntimeException("Database error updating question: " + question.getId(), e);
            }
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM questions WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting question: " + id, e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM questions";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting questions", e);
        }
        return 0;
    }

    private List<Option> findOptionsByQuestionId(Connection conn, Long questionId) throws SQLException {
        String sql = "SELECT id, option_text, is_correct, question_id FROM options WHERE question_id = ? ORDER BY id ASC";
        List<Option> options = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Option o = new Option();
                    o.setId(rs.getLong("id"));
                    o.setOptionText(rs.getString("option_text"));
                    o.setIsCorrect(rs.getBoolean("is_correct"));
                    options.add(o);
                }
            }
        }
        return options;
    }

    private Question mapRowToQuestion(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getLong("id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setQuestionType(QuestionType.valueOf(rs.getString("question_type")));
        q.setMarks(rs.getInt("marks"));
        q.setExplanation(rs.getString("explanation"));

        Quiz quiz = new Quiz();
        quiz.setId(rs.getLong("quiz_id"));
        q.setQuiz(quiz);

        return q;
    }
}
