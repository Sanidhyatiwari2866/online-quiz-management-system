package com.quiz.model;

import jakarta.persistence.*;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private QuizAttempt quizAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option_ids", length = 255)
    private String selectedOptionIds; // Comma separated IDs of options picked by the user

    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect = false;

    @Column(name = "marks_awarded", nullable = false)
    private Integer marksAwarded = 0;

    public AttemptAnswer() {
    }

    public AttemptAnswer(QuizAttempt quizAttempt, Question question, String selectedOptionIds, Boolean isCorrect, Integer marksAwarded) {
        this.quizAttempt = quizAttempt;
        this.question = question;
        this.selectedOptionIds = selectedOptionIds;
        this.isCorrect = isCorrect != null ? isCorrect : false;
        this.marksAwarded = marksAwarded != null ? marksAwarded : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public QuizAttempt getQuizAttempt() {
        return quizAttempt;
    }

    public void setQuizAttempt(QuizAttempt quizAttempt) {
        this.quizAttempt = quizAttempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public String getSelectedOptionIds() {
        return selectedOptionIds;
    }

    public void setSelectedOptionIds(String selectedOptionIds) {
        this.selectedOptionIds = selectedOptionIds;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }
}
