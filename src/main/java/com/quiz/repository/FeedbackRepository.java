package com.quiz.repository;

import com.quiz.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByQuizIdOrderByCreatedAtDesc(Long quizId);

    List<Feedback> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.quiz.id = :quizId")
    Double getAverageRatingByQuizId(@Param("quizId") Long quizId);
}
