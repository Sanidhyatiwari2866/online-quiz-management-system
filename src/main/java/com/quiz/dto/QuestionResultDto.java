package com.quiz.dto;

import com.quiz.model.QuestionType;
import java.util.ArrayList;
import java.util.List;

public class QuestionResultDto {
    private Long questionId;
    private String questionText;
    private QuestionType questionType;
    private Integer marks;
    private Integer marksAwarded;
    private Boolean isCorrect;
    private String explanation;
    private List<OptionDto> options = new ArrayList<>();
    private List<Long> selectedOptionIds = new ArrayList<>();
    private List<Long> correctOptionIds = new ArrayList<>();

    public QuestionResultDto() {
    }

    public QuestionResultDto(Long questionId, String questionText, QuestionType questionType, Integer marks,
                             Integer marksAwarded, Boolean isCorrect, String explanation, List<OptionDto> options,
                             List<Long> selectedOptionIds, List<Long> correctOptionIds) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks;
        this.marksAwarded = marksAwarded;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
        this.options = options != null ? options : new ArrayList<>();
        this.selectedOptionIds = selectedOptionIds != null ? selectedOptionIds : new ArrayList<>();
        this.correctOptionIds = correctOptionIds != null ? correctOptionIds : new ArrayList<>();
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {
        this.options = options;
    }

    public List<Long> getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(List<Long> selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    public List<Long> getCorrectOptionIds() {
        return correctOptionIds;
    }

    public void setCorrectOptionIds(List<Long> correctOptionIds) {
        this.correctOptionIds = correctOptionIds;
    }
}
