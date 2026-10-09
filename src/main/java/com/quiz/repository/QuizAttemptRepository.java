package com.quiz.repository;

import com.quiz.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserIdOrderByStartedAtDesc(Long userId);
    List<QuizAttempt> findByQuizIdOrderByStartedAtDesc(Long quizId);
    long countByQuizId(Long quizId);

    @Query("SELECT COUNT(a) FROM QuizAttempt a WHERE a.quiz.id = :quizId AND a.passed = true")
    long countPassedByQuizId(@Param("quizId") Long quizId);

    @Query("SELECT AVG(a.score) FROM QuizAttempt a WHERE a.quiz.id = :quizId")
    Double getAverageScoreByQuizId(@Param("quizId") Long quizId);
}
