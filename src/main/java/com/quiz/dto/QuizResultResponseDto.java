package com.quiz.dto;

import com.quiz.model.AttemptStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizResultResponseDto {
    private Long attemptId;
    private Long quizId;
    private String quizTitle;
    private String studentName;
    private Integer score;
    private Integer totalMarks;
    private Integer totalQuestions;
    private Integer correctAnswers;
    private Double percentage;
    private Boolean passed;
    private Integer passingScore;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Long timeTakenSeconds;
    private AttemptStatus status;
    private List<QuestionResultDto> questionResults = new ArrayList<>();

    public QuizResultResponseDto() {
    }

    public QuizResultResponseDto(Long attemptId, Long quizId, String quizTitle, String studentName,
                                 Integer score, Integer totalMarks, Integer totalQuestions, Integer correctAnswers,
                                 Double percentage, Boolean passed, Integer passingScore,
                                 LocalDateTime startedAt, LocalDateTime completedAt, Long timeTakenSeconds,
                                 AttemptStatus status, List<QuestionResultDto> questionResults) {
        this.attemptId = attemptId;
        this.quizId = quizId;
        this.quizTitle = quizTitle;
        this.studentName = studentName;
        this.score = score;
        this.totalMarks = totalMarks;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.percentage = percentage;
        this.passed = passed;
        this.passingScore = passingScore;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.timeTakenSeconds = timeTakenSeconds;
        this.status = status;
        this.questionResults = questionResults != null ? questionResults : new ArrayList<>();
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

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Long getTimeTakenSeconds() {
        return timeTakenSeconds;
    }

    public void setTimeTakenSeconds(Long timeTakenSeconds) {
        this.timeTakenSeconds = timeTakenSeconds;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public List<QuestionResultDto> getQuestionResults() {
        return questionResults;
    }

    public void setQuestionResults(List<QuestionResultDto> questionResults) {
        this.questionResults = questionResults;
    }
}
