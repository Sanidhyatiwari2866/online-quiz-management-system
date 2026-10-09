package com.quiz.repository;

import com.quiz.model.Quiz;
import com.quiz.model.QuizStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

    List<Quiz> findByCategoryId(Long categoryId);

    List<Quiz> findByStatus(QuizStatus status);

    List<Quiz> findByStatusAndCategoryId(QuizStatus status, Long categoryId);

    @Query("SELECT q FROM Quiz q WHERE q.status = :status AND (LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Quiz> searchApprovedByKeyword(@Param("status") QuizStatus status, @Param("keyword") String keyword);

    @Query("SELECT q FROM Quiz q WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(q.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Quiz> searchByKeyword(@Param("keyword") String keyword);

    List<Quiz> findByCreatorId(Long creatorId);

    long countByStatus(QuizStatus status);
}
