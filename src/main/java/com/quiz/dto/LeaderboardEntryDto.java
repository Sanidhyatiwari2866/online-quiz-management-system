package com.quiz.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for public and quiz-specific leaderboards.
 */
public class LeaderboardEntryDto {

    private Long userId;
    private String username;
    private String fullName;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Integer totalMarks;
    private Double percentage;
    private Long timeTakenSeconds;
    private Integer rank;
    private LocalDateTime completedAt;

    public LeaderboardEntryDto() {
    }

    public LeaderboardEntryDto(Long userId, String username, String fullName, Long quizId, String quizTitle,
                               Integer score, Integer totalMarks, Double percentage, Long timeTakenSeconds,
                               Integer rank, LocalDateTime completedAt) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.score = score;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.timeTakenSeconds = timeTakenSeconds;
        this.rank = rank;
        this.completedAt = completedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Long getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Long timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
