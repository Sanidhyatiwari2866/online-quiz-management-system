package com.quiz.controller;

import com.quiz.dto.*;
import com.quiz.model.User;
import com.quiz.service.AuthService;
import com.quiz.service.QuizAttemptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;
    private final AuthService authService;

    public QuizAttemptController(QuizAttemptService attemptService, AuthService authService) {
        this.attemptService = attemptService;
        this.authService = authService;
    }

    @PostMapping("/start/{quizId}")
    public ResponseEntity<ApiResponse<QuizStartResponseDto>> startQuiz(@PathVariable Long quizId) {
        User user = authService.getCurrentAuthenticatedUser();
        QuizStartResponseDto startDto = attemptService.startQuiz(quizId, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz started successfully", startDto));
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<QuizResultResponseDto>> submitQuiz(@Valid @RequestBody QuizSubmitRequestDto submission) {
        User user = authService.getCurrentAuthenticatedUser();
        QuizResultResponseDto resultDto = attemptService.submitQuiz(submission, user);
        return ResponseEntity.ok(ApiResponse.success("Quiz submitted successfully", resultDto));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<ApiResponse<QuizResultResponseDto>> getAttemptDetails(@PathVariable Long attemptId) {
        User user = authService.getCurrentAuthenticatedUser();
        QuizResultResponseDto resultDto = attemptService.getAttemptResult(attemptId, user);
        return ResponseEntity.ok(ApiResponse.success("Attempt details retrieved successfully", resultDto));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AttemptSummaryDto>>> getMyAttempts() {
        User user = authService.getCurrentAuthenticatedUser();
        List<AttemptSummaryDto> attempts = attemptService.getUserAttempts(user.getId());
        return ResponseEntity.ok(ApiResponse.success("User attempts retrieved successfully", attempts));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER') or #userId == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<AttemptSummaryDto>>> getUserAttempts(@PathVariable Long userId) {
        List<AttemptSummaryDto> attempts = attemptService.getUserAttempts(userId);
        return ResponseEntity.ok(ApiResponse.success("User attempts retrieved successfully", attempts));
    }

    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<AttemptSummaryDto>>> getQuizAttempts(@PathVariable Long quizId) {
        List<AttemptSummaryDto> attempts = attemptService.getQuizAttempts(quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz attempts retrieved successfully", attempts));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<List<AttemptSummaryDto>>> getAllAttempts() {
        List<AttemptSummaryDto> attempts = attemptService.getAllAttempts();
        return ResponseEntity.ok(ApiResponse.success("All attempts retrieved successfully", attempts));
    }

    @GetMapping("/analytics")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getAnalytics() {
        AnalyticsSummaryDto analytics = attemptService.getAnalyticsSummary();
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", analytics));
    }
}
