package com.quiz.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quiz.model.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuestionDto {
    private Long id;

    @NotBlank(message = "Question text is required")
    private String questionText;

    @NotNull(message = "Question type is required")
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    private Integer marks = 1;

    private String explanation;

    private Long quizId;

    private List<OptionDto> options = new ArrayList<>();

    public QuestionDto() {
    }

    public QuestionDto(Long id, String questionText, QuestionType questionType, Integer marks, String explanation, Long quizId, List<OptionDto> options) {
        this.id = id;
        this.questionText = questionText;
        this.questionType = questionType;
        this.marks = marks;
        this.explanation = explanation;
        this.quizId = quizId;
        this.options = options != null ? options : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public List<OptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {
        this.options = options;
    }
}
