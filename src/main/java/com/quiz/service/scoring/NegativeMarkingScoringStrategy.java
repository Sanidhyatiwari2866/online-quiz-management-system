package com.quiz.service.scoring;

import com.quiz.model.Question;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Competitive examination scoring strategy: awards positive marks for correct,
 * applies -1 mark penalty for wrong attempts, and 0 for unattempted questions.
 */
@Component("negativeMarkingScoringStrategy")
public class NegativeMarkingScoringStrategy implements ScoringStrategy {

    @Override
    public ScoreResult evaluate(Question question, Set<Long> selectedOptionIds, Set<Long> correctOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            return new ScoreResult(false, 0, "Unanswered (No penalty)");
        }

        boolean isExactMatch = correctOptionIds.equals(selectedOptionIds);
        int marks = question.getMarks() != null && question.getMarks() > 0 ? question.getMarks() : 1;

        if (isExactMatch) {
            return new ScoreResult(true, marks, "Correct answer");
        } else {
            // Apply negative penalty (-1 mark) for incorrect attempt
            return new ScoreResult(false, -1, "Incorrect attempt (-1 penalty)");
        }
    }

    @Override
    public String getStrategyName() {
        return "NEGATIVE_MARKING";
    }
}
