package com.quiz.dto;

import java.time.LocalDateTime;
import java.util.List;

public class QuizStartResponseDto {
    private Long attemptId;
    private Long quizId;
    private String title;
    private String description;
    private Integer timeLimitMinutes;
    private Integer totalMarks;
    private Integer passingScore;
    private LocalDateTime startedAt;
    private List<QuestionDto> questions;

    public QuizStartResponseDto() {
    }

    public QuizStartResponseDto(Long attemptId, Long quizId, String title, String description,
                               Integer timeLimitMinutes, Integer totalMarks, Integer passingScore,
                               LocalDateTime startedAt, List<QuestionDto> questions) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.title = title;
        this.description = description;
        this.timeLimitMinutes = timeLimitMinutes;
        this.totalMarks = totalMarks;
        this.passingScore = passingScore;
        this.startedAt = startedAt;
        this.questions = questions;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
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

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public List<QuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionDto> questions) {
        this.questions = questions;
    }
}
