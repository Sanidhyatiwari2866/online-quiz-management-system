package com.quiz.dto;

import java.util.ArrayList;
import java.util.List;

public class AnswerSubmissionDto {
    private Long questionId;
    private List<Long> selectedOptionIds = new ArrayList<>();

    public AnswerSubmissionDto() {
    }

    public AnswerSubmissionDto(Long questionId, List<Long> selectedOptionIds) {
        this.questionId = questionId;
        this.selectedOptionIds = selectedOptionIds != null ? selectedOptionIds : new ArrayList<>();
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public List<Long> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(List<Long> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }
}
