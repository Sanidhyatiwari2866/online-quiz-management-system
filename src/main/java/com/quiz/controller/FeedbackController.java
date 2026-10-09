package com.quiz.controller;

import com.quiz.dto.ApiResponse;
import com.quiz.dto.FeedbackDto;
import com.quiz.dto.FeedbackRequestDto;
import com.quiz.model.User;
import com.quiz.service.AuthService;
import com.quiz.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AuthService authService;

    public FeedbackController(FeedbackService feedbackService, AuthService authService) {
        this.feedbackService = feedbackService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackDto>> submitFeedback(@Valid @RequestBody FeedbackRequestDto request) {
        User user = authService.getCurrentAuthenticatedUser();
        FeedbackDto saved = feedbackService.submitFeedback(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Feedback submitted successfully", saved));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getQuizFeedback(@PathVariable Long quizId) {
        List<FeedbackDto> feedbackList = feedbackService.getFeedbackForQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz feedback retrieved successfully", feedbackList));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getMyFeedback() {
        User user = authService.getCurrentAuthenticatedUser();
        List<FeedbackDto> feedbackList = feedbackService.getFeedbackForUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success("User feedback retrieved successfully", feedbackList));
    }
}
