package com.quiz.dto;

public class AnalyticsSummaryDto {
    private long totalQuizzes;
    private long totalCategories;
    private long totalQuestions;
    private long totalStudents;
    private long totalAttempts;
    private double passRatePercentage;

    public AnalyticsSummaryDto() {
    }

    public AnalyticsSummaryDto(long totalQuizzes, long totalCategories, long totalQuestions,
                               long totalStudents, long totalAttempts, double passRatePercentage) {
        this.totalQuizzes = totalQuizzes;
        this.totalCategories = totalCategories;
        this.totalQuestions = totalQuestions;
        this.totalStudents = totalStudents;
        this.totalAttempts = totalAttempts;
        this.passRatePercentage = passRatePercentage;
    }

    public long getTotalQuizzes() {
        return totalQuizzes;
    }

    public void setTotalQuizzes(long totalQuizzes) {
        this.totalQuizzes = totalQuizzes;
    }

    public long getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(long totalCategories) {
        this.totalCategories = totalCategories;
    }

    public long getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(long totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public double getPassRatePercentage() {
        return passRatePercentage;
    }

    public void setPassRatePercentage(double passRatePercentage) {
        this.passRatePercentage = passRatePercentage;
    }
}
