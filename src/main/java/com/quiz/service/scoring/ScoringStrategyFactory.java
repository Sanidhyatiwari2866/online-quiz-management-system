package com.quiz.service.scoring;

import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * Factory class utilizing Map collection and Polymorphism to resolve appropriate ScoringStrategy.
 */
@Component
public class ScoringStrategyFactory {

    private final Map<String, ScoringStrategy> strategies;

    public ScoringStrategyFactory(Map<String, ScoringStrategy> strategies) {
        this.strategies = strategies;
    }

    public ScoringStrategy getStrategy(String strategyName) {
        if ("NEGATIVE_MARKING".equalsIgnoreCase(strategyName)) {
            return strategies.getOrDefault("negativeMarkingScoringStrategy", strategies.get("standardScoringStrategy"));
        }
        return strategies.get("standardScoringStrategy");
    }
}
