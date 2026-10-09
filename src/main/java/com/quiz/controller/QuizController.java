package com.quiz.controller;

import com.quiz.dto.ApiResponse;
import com.quiz.dto.QuestionDto;
import com.quiz.dto.QuizRequestDto;
import com.quiz.dto.QuizResponseDto;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.service.AuthService;
import com.quiz.service.QuestionService;
import com.quiz.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final AuthService authService;

    public QuizController(QuizService quizService,
                          QuestionService questionService,
                          AuthService authService) {
        this.quizService = quizService;
        this.questionService = questionService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<QuizResponseDto>>> getAllQuizzes(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "false") boolean includeAll) {

        // If user is Admin or Teacher and requests all, include non-approved ones
        boolean canViewAll = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            canViewAll = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()) || a.getAuthority().equals(Role.ROLE_TEACHER.name()));
        }

        List<QuizResponseDto> quizzes = quizService.getAllQuizzes(categoryId, keyword, includeAll && canViewAll);
        return ResponseEntity.ok(ApiResponse.success("Quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<List<QuizResponseDto>>> getPendingQuizzes() {
        List<QuizResponseDto> quizzes = quizService.getPendingQuizzes();
        return ResponseEntity.ok(ApiResponse.success("Pending quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuizResponseDto>> getQuizById(@PathVariable Long id) {
        QuizResponseDto quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz retrieved successfully", quiz));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<QuizResponseDto>> createQuiz(@Valid @RequestBody QuizRequestDto dto) {
        User creator = authService.getCurrentAuthenticatedUser();
        QuizResponseDto created = quizService.createQuiz(dto, creator);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Quiz created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<QuizResponseDto>> updateQuiz(@PathVariable Long id, @Valid @RequestBody QuizRequestDto dto) {
        QuizResponseDto updated = quizService.updateQuiz(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Quiz updated successfully", updated));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<QuizResponseDto>> approveQuiz(@PathVariable Long id) {
        QuizResponseDto approved = quizService.approveQuiz(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz approved successfully", approved));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<QuizResponseDto>> rejectQuiz(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : "Does not meet platform quality standards";
        QuizResponseDto rejected = quizService.rejectQuiz(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Quiz rejected", rejected));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<Void>> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted successfully"));
    }

    @GetMapping("/{id}/questions")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getQuestions(@PathVariable Long id) {
        boolean isStudentView = true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            boolean isStaff = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals(Role.ROLE_ADMIN.name()) || a.getAuthority().equals(Role.ROLE_TEACHER.name()));
            if (isStaff) {
                isStudentView = false;
            }
        }

        List<QuestionDto> questions = quizService.getQuestionsForQuiz(id, isStudentView);
        return ResponseEntity.ok(ApiResponse.success("Questions retrieved successfully", questions));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_TEACHER')")
    public ResponseEntity<ApiResponse<QuestionDto>> addQuestion(@PathVariable Long id, @Valid @RequestBody QuestionDto dto) {
        QuestionDto created = questionService.addQuestionToQuiz(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Question added successfully", created));
    }
}
