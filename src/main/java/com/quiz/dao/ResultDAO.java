package com.quiz.dao;

import com.quiz.dto.LeaderboardEntryDto;
import com.quiz.model.QuizAttempt;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Quiz results and attempts.
 */
public interface ResultDAO {

    Optional<QuizAttempt> findById(Long id);

    List<QuizAttempt> findByUserId(Long userId);

    List<QuizAttempt> findByQuizId(Long quizId);

    List<QuizAttempt> findAll();

    List<LeaderboardEntryDto> getGlobalLeaderboard(int limit);

    List<LeaderboardEntryDto> getQuizLeaderboard(Long quizId, int limit);

    long count();
}
