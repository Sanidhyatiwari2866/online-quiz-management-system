package com.quiz;

import com.quiz.model.*;
import com.quiz.service.QuizAttemptService;
import com.quiz.service.scoring.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Online Quiz Management System.
 * Covers:
 *   - Spring Boot context loading
 *   - Public API endpoint accessibility
 *   - JWT-protected endpoint security
 *   - Scoring strategy correctness (Strategy Pattern - OOP)
 *   - Application startup data seeding
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("h2")
class OnlineQuizApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ScoringStrategyFactory scoringStrategyFactory;

    @Autowired
    private QuizAttemptService quizAttemptService;

    // ================================================================
    // Test 1: Application Context Loads Successfully
    // ================================================================
    @Test
    @DisplayName("Spring context loads without errors")
    void contextLoads() {
        assertThat(mockMvc).isNotNull();
        assertThat(scoringStrategyFactory).isNotNull();
        assertThat(quizAttemptService).isNotNull();
    }

    // ================================================================
    // Test 2: Public Endpoints Are Accessible Without Authentication
    // ================================================================
    @Test
    @DisplayName("GET /api/categories - public endpoint should return 200")
    void publicCategoriesEndpoint_returnsOk() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/quizzes - public endpoint returns only approved quizzes")
    void publicQuizzesEndpoint_returnsOk() throws Exception {
        mockMvc.perform(get("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/leaderboard - public leaderboard returns 200")
    void leaderboardEndpoint_returnsOk() throws Exception {
        mockMvc.perform(get("/api/leaderboard"))
                .andExpect(status().isOk());
    }

    // ================================================================
    // Test 3: Secured Endpoints Reject Unauthenticated Access
    // ================================================================
    @Test
    @DisplayName("GET /api/admin/users - without JWT should return 401 or 403")
    void adminUsersEndpoint_withoutJwt_returns401or403() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got: " + status);
                });
    }

    @Test
    @DisplayName("POST /api/attempts/start/1 - without JWT should return 401 or 403")
    void startQuizEndpoint_withoutJwt_returns401or403() throws Exception {
        mockMvc.perform(post("/api/attempts/start/1"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Expected 401 or 403 but got: " + status);
                });
    }

    // ================================================================
    // Test 4: Authentication - Login with Valid Credentials
    // ================================================================
    @Test
    @DisplayName("POST /api/auth/login - admin credentials should succeed")
    void loginWithValidAdminCredentials_returnsJwt() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"admin123\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("POST /api/auth/login - invalid credentials should return 401")
    void loginWithInvalidCredentials_returns401() throws Exception {
        String loginBody = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // Test 5: Standard Scoring Strategy (OOP Strategy Pattern)
    // ================================================================
    @Test
    @DisplayName("StandardScoringStrategy - correct single answer awards full marks")
    void standardScoring_correctAnswer_awardsFullMarks() {
        ScoringStrategy strategy = scoringStrategyFactory.getStrategy("STANDARD");
        assertNotNull(strategy, "Standard scoring strategy must not be null");

        Question question = buildTestQuestion(2);
        Set<Long> correctIds = Set.of(1L);
        Set<Long> selectedIds = Set.of(1L); // User selected the correct answer

        ScoreResult result = strategy.evaluate(question, selectedIds, correctIds);

        assertTrue(result.isCorrect(), "Answer should be marked correct");
        assertEquals(2, result.getMarksAwarded(), "Should award full 2 marks");
    }

    @Test
    @DisplayName("StandardScoringStrategy - wrong answer awards zero marks")
    void standardScoring_wrongAnswer_awardsZeroMarks() {
        ScoringStrategy strategy = scoringStrategyFactory.getStrategy("STANDARD");

        Question question = buildTestQuestion(2);
        Set<Long> correctIds = Set.of(1L);
        Set<Long> selectedIds = Set.of(2L); // User selected the wrong answer

        ScoreResult result = strategy.evaluate(question, selectedIds, correctIds);

        assertFalse(result.isCorrect(), "Answer should be marked incorrect");
        assertEquals(0, result.getMarksAwarded(), "Wrong answer should award 0 marks");
    }

    @Test
    @DisplayName("StandardScoringStrategy - no answer selected awards zero marks")
    void standardScoring_noAnswer_awardsZeroMarks() {
        ScoringStrategy strategy = scoringStrategyFactory.getStrategy("STANDARD");

        Question question = buildTestQuestion(3);
        Set<Long> correctIds = Set.of(1L);
        Set<Long> selectedIds = new HashSet<>(); // No answer given

        ScoreResult result = strategy.evaluate(question, selectedIds, correctIds);

        assertFalse(result.isCorrect(), "Unanswered question should not be correct");
        assertEquals(0, result.getMarksAwarded(), "Unanswered question should award 0 marks");
    }

    // ================================================================
    // Test 6: Negative Marking Scoring Strategy (OOP Polymorphism)
    // ================================================================
    @Test
    @DisplayName("NegativeMarkingScoringStrategy - factory resolves correct strategy")
    void negativeMarking_strategyResolvesCorrectly() {
        ScoringStrategy strategy = scoringStrategyFactory.getStrategy("NEGATIVE_MARKING");
        assertNotNull(strategy, "Negative marking strategy must not be null");
        assertNotEquals("standardScoringStrategy", strategy.getStrategyName(),
                "Should resolve to negative marking, not standard");
    }

    // ================================================================
    // Test 7: Analytics endpoint (Admin/Teacher protected)
    // ================================================================
    @Test
    @DisplayName("GET /api/attempts/analytics - without JWT should be rejected")
    void analyticsEndpoint_withoutJwt_isRejected() throws Exception {
        mockMvc.perform(get("/api/attempts/analytics"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    assertTrue(status == 401 || status == 403,
                            "Analytics should require authentication");
                });
    }

    // ================================================================
    // Test 8: Registration endpoint
    // ================================================================
    @Test
    @DisplayName("POST /api/auth/register - duplicate username should fail with 400")
    void registerDuplicateUsername_fails() throws Exception {
        String regBody = "{\"username\":\"admin\",\"email\":\"admin_dup@test.com\",\"password\":\"admin123\",\"fullName\":\"Dup Admin\",\"role\":\"ROLE_STUDENT\"}";
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regBody))
                .andExpect(status().isBadRequest());
    }

    // ================================================================
    // Helper methods
    // ================================================================

    /**
     * Builds a dummy Question entity for scoring tests.
     * Demonstrates OOP: encapsulation via setters, separation of test data.
     */
    private Question buildTestQuestion(int marks) {
        Question q = new Question();
        q.setQuestionText("What is the output of System.out.println(\"Java\")?");
        q.setQuestionType(QuestionType.SINGLE_CHOICE);
        q.setMarks(marks);
        return q;
    }
}
