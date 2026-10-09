package com.quiz.dao;

import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Quiz entities.
 */
public interface QuizDAO {

    Optional<Quiz> findById(Long id);

    List<Quiz> findAll();

    List<Quiz> findByStatus(QuizStatus status);

    Quiz save(Quiz quiz);

    boolean updateStatus(Long quizId, QuizStatus status, String rejectionReason);

    boolean deleteById(Long id);

    long count();
}
