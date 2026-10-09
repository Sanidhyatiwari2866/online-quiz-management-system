package com.quiz.service.scoring;

/**
 * Encapsulates the polymorphic evaluation result for a single question attempt.
 */
public class ScoreResult {

    private final boolean correct;
    private final int marksAwarded;
    private final String remarks;

    public ScoreResult(boolean correct, int marksAwarded, String remarks) {
        this.correct = correct;
        this.marksAwarded = marksAwarded;
        this.remarks = remarks;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getMarksAwarded() {
        return marksAwarded;
    }

    public String getRemarks() {
        return remarks;
    }
}
