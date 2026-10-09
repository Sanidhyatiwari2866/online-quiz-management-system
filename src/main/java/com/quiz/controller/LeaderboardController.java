package com.quiz.controller;

import com.quiz.dao.ResultDAO;
import com.quiz.dto.ApiResponse;
import com.quiz.dto.LeaderboardEntryDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final ResultDAO resultDAO;

    public LeaderboardController(@Qualifier("resultJdbcDAO") ResultDAO resultDAO) {
        this.resultDAO = resultDAO;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDto> leaderboard = resultDAO.getGlobalLeaderboard(limit);
        return ResponseEntity.ok(ApiResponse.success("Global leaderboard retrieved successfully", leaderboard));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getQuizLeaderboard(
            @PathVariable Long quizId,
            @RequestParam(defaultValue = "10") int limit) {
        List<LeaderboardEntryDto> leaderboard = resultDAO.getQuizLeaderboard(quizId, limit);
        return ResponseEntity.ok(ApiResponse.success("Quiz leaderboard retrieved successfully", leaderboard));
    }
}
