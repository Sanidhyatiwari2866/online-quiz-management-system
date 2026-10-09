package com.quiz.dto;

import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class QuizSubmitRequestDto {

    @NotNull(message = "Attempt ID is required")
    private Long attemptId;

    private List<AnswerSubmissionDto> answers = new ArrayList<>();

    public QuizSubmitRequestDto() {
    }

    public QuizSubmitRequestDto(Long attemptId, List<AnswerSubmissionDto> answers) {
        this.attemptId = attemptId;
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public List<AnswerSubmissionDto> answers() {
        return answers;
    }

    public List<AnswerSubmissionDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmissionDto> answers) {
        this.answers = answers;
    }
}
