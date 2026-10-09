package com.quiz.service.scoring;

import com.quiz.model.Question;
import java.util.Set;

/**
 * Strategy interface demonstrating OOP Polymorphism and Abstraction.
 * Allows pluggable question scoring algorithms (e.g. Standard, Negative Marking, Partial Credit).
 */
public interface ScoringStrategy {

    ScoreResult evaluate(Question question, Set<Long> selectedOptionIds, Set<Long> correctOptionIds);

    String getStrategyName();
}
