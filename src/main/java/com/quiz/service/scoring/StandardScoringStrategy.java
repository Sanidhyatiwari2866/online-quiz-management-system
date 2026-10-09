package com.quiz.service.scoring;

import com.quiz.model.Question;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Standard scoring strategy: awards full question marks for exact match, 0 otherwise.
 */
@Component("standardScoringStrategy")
public class StandardScoringStrategy implements ScoringStrategy {

    @Override
    public ScoreResult evaluate(Question question, Set<Long> selectedOptionIds, Set<Long> correctOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            return new ScoreResult(false, 0, "Unanswered");
        }

        boolean isExactMatch = correctOptionIds.equals(selectedOptionIds);
        int marks = question.getMarks() != null && question.getMarks() > 0 ? question.getMarks() : 1;

        if (isExactMatch) {
            return new ScoreResult(true, marks, "Correct answer");
        } else {
            return new ScoreResult(false, 0, "Incorrect answer");
        }
    }

    @Override
    public String getStrategyName() {
        return "STANDARD";
    }
}
