package com.quiz.dto;

import com.quiz.model.QuizStatus;
import java.time.LocalDateTime;

public class QuizResponseDto {
    private Long id;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer totalMarks;
    private Integer passingScore;
    private Boolean randomizeQuestions;
    private Long categoryId;
    private String categoryName;
    private int questionCount;
    private String createdBy;
    private QuizStatus status = QuizStatus.APPROVED;
    private String rejectionReason;
    private Double averageRating = 0.0;
    private LocalDateTime createdAt;

    public QuizResponseDto() {
    }

    public QuizResponseDto(Long id, String title, String description, Integer timeLimitMinutes, Integer totalMarks,
                           Integer passingScore, Boolean randomizeQuestions, Long categoryId, String categoryName,
                           int questionCount, String createdBy, QuizStatus status, String rejectionReason,
                           Double averageRating, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.timeLimitMinutes = timeLimitMinutes;
        this.totalMarks = totalMarks;
        this.passingScore = passingScore;
        this.randomizeQuestions = randomizeQuestions;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.questionCount = questionCount;
        this.createdBy = createdBy;
        this.status = status != null ? status : QuizStatus.APPROVED;
        this.rejectionReason = rejectionReason;
        this.averageRating = averageRating != null ? averageRating : 0.0;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Integer getPassingScore() {
        return passingScore;
    }

    public void setPassingScore(Integer passingScore) {
        this.passingScore = passingScore;
    }

    public Boolean getRandomizeQuestions() {
        return randomizeQuestions;
    }

    public void setRandomizeQuestions(Boolean randomizeQuestions) {
        this.randomizeQuestions = randomizeQuestions;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
