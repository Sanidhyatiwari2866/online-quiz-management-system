package com.quiz.dao;

import com.quiz.model.Question;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Question entities.
 */
public interface QuestionDAO {

    Optional<Question> findById(Long id);

    List<Question> findByQuizId(Long quizId);

    Question save(Question question);

    boolean deleteById(Long id);

    long count();
}
