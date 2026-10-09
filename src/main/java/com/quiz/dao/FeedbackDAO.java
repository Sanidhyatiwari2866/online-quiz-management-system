package com.quiz.dao;

import com.quiz.model.Feedback;
import java.util.List;

/**
 * Data Access Object (DAO) interface for participant Feedback.
 */
public interface FeedbackDAO {

    Feedback save(Feedback feedback);

    List<Feedback> findByQuizId(Long quizId);

    List<Feedback> findByUserId(Long userId);

    double getAverageRatingForQuiz(Long quizId);

    long count();
}
