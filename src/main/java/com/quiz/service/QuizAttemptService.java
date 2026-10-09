package com.quiz.service;

import com.quiz.dto.*;
import com.quiz.exception.BadRequestException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.exception.UnauthorizedException;
import com.quiz.model.*;
import com.quiz.repository.*;
import com.quiz.service.scoring.ScoreResult;
import com.quiz.service.scoring.ScoringStrategy;
import com.quiz.service.scoring.ScoringStrategyFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizAttemptService {

    private final QuizAttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizService quizService;
    private final ScoringStrategyFactory scoringStrategyFactory;

    public QuizAttemptService(QuizAttemptRepository attemptRepository,
                              AttemptAnswerRepository attemptAnswerRepository,
                              QuizRepository quizRepository,
                              UserRepository userRepository,
                              CategoryRepository categoryRepository,
                              QuestionRepository questionRepository,
                              QuizService quizService,
                              ScoringStrategyFactory scoringStrategyFactory) {
        this.attemptRepository = attemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizService = quizService;
        this.scoringStrategyFactory = scoringStrategyFactory;
    }

    @Transactional
    public QuizStartResponseDto startQuiz(Long quizId, User user) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        if (quiz.getStatus() != QuizStatus.APPROVED) {
            throw new BadRequestException("This quiz is currently " + quiz.getStatus() + " and cannot be attempted.");
        }

        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new BadRequestException("This quiz has no questions yet and cannot be taken.");
        }

        // Create new attempt record
        QuizAttempt attempt = new QuizAttempt(user, quiz);
        attempt = attemptRepository.save(attempt);

        // Fetch questions (student view hides answers and explanations)
        List<QuestionDto> questions = quizService.getQuestionsForQuiz(quizId, true);

        return new QuizStartResponseDto(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTimeLimitMinutes(),
                quiz.getTotalMarks(),
                quiz.getPassingScore(),
                attempt.getStartedAt(),
                questions
        );
    }

    @Transactional
    public QuizResultResponseDto submitQuiz(QuizSubmitRequestDto submission, User user) {
        QuizAttempt attempt = attemptRepository.findById(submission.getAttemptId())
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + submission.getAttemptId()));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("You are not authorized to submit this attempt");
        }

        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BadRequestException("This quiz attempt has already been submitted or completed");
        }

        Quiz quiz = attempt.getQuiz();
        LocalDateTime now = LocalDateTime.now();
        attempt.setCompletedAt(now);

        long timeTakenSeconds = Duration.between(attempt.getStartedAt(), now).getSeconds();
        attempt.setTimeTakenSeconds(Math.max(0, timeTakenSeconds));

        // Time Limit Validation (with 60-second network latency grace window)
        long maxAllowedSeconds = (quiz.getTimeLimitMinutes() * 60L) + 60L;
        boolean isTimedOut = timeTakenSeconds > maxAllowedSeconds;
        attempt.setStatus(isTimedOut ? AttemptStatus.TIMED_OUT : AttemptStatus.COMPLETED);

        // Map submitted answers by questionId
        Map<Long, List<Long>> answersMap = new HashMap<>();
        if (submission.getAnswers() != null) {
            for (AnswerSubmissionDto ans : submission.getAnswers()) {
                answersMap.put(ans.getQuestionId(), ans.getSelectedOptionIds() != null ? ans.getSelectedOptionIds() : Collections.emptyList());
            }
        }

        int totalEarnedScore = 0;
        int correctCount = 0;
        List<QuestionResultDto> questionResults = new ArrayList<>();

        // Resolve polymorphic scoring strategy
        ScoringStrategy scoringStrategy = scoringStrategyFactory.getStrategy("STANDARD");

        for (Question question : quiz.getQuestions()) {
            List<Long> userSelectedIds = answersMap.getOrDefault(question.getId(), Collections.emptyList());

            // Get correct option IDs for this question
            Set<Long> correctOptionIds = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(Option::getId)
                    .collect(Collectors.toSet());

            Set<Long> selectedSet = new HashSet<>(userSelectedIds);

            // Polymorphic evaluation using OOP strategy pattern
            ScoreResult scoreResult = scoringStrategy.evaluate(question, selectedSet, correctOptionIds);
            boolean isQuestionCorrect = scoreResult.isCorrect();
            int marksAwarded = scoreResult.getMarksAwarded();

            if (isQuestionCorrect) {
                totalEarnedScore += marksAwarded;
                correctCount++;
            }

            // Save AttemptAnswer
            String selectedOptionsStr = userSelectedIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            AttemptAnswer answerRecord = new AttemptAnswer(attempt, question, selectedOptionsStr, isQuestionCorrect, marksAwarded);
            attempt.addAnswer(answerRecord);

            // Construct QuestionResultDto
            List<OptionDto> optionDtos = question.getOptions().stream()
                    .map(o -> new OptionDto(o.getId(), o.getOptionText(), o.getIsCorrect()))
                    .collect(Collectors.toList());

            questionResults.add(new QuestionResultDto(
                    question.getId(),
                    question.getQuestionText(),
                    question.getQuestionType(),
                    question.getMarks(),
                    marksAwarded,
                    isQuestionCorrect,
                    question.getExplanation(),
                    optionDtos,
                    userSelectedIds,
                    new ArrayList<>(correctOptionIds)
            ));
        }

        attempt.setScore(Math.max(0, totalEarnedScore));
        attempt.setCorrectAnswers(correctCount);
        attempt.setTotalQuestions(quiz.getQuestions().size());
        attempt.setTotalMarks(quiz.getTotalMarks() != null && quiz.getTotalMarks() > 0 ? quiz.getTotalMarks() : quiz.getQuestions().size());

        double percentage = attempt.getTotalMarks() > 0
                ? ((double) attempt.getScore() / attempt.getTotalMarks()) * 100.0
                : 0.0;

        boolean passed = percentage >= (quiz.getPassingScore() != null ? quiz.getPassingScore() : 50);
        attempt.setPassed(passed);

        attempt = attemptRepository.save(attempt);

        return new QuizResultResponseDto(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                attempt.getScore(),
                attempt.getTotalMarks(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                Math.round(percentage * 100.0) / 100.0,
                attempt.getPassed(),
                quiz.getPassingScore(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getTimeTakenSeconds(),
                attempt.getStatus(),
                questionResults
        );
    }

    @Transactional(readOnly = true)
    public QuizResultResponseDto getAttemptResult(Long attemptId, User currentUser) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        boolean isOwner = attempt.getUser().getId().equals(currentUser.getId());
        boolean isStaff = currentUser.getRole() == Role.ROLE_ADMIN || currentUser.getRole() == Role.ROLE_TEACHER;

        if (!isOwner && !isStaff) {
            throw new UnauthorizedException("You are not authorized to view this quiz attempt");
        }

        Quiz quiz = attempt.getQuiz();
        List<AttemptAnswer> answers = attemptAnswerRepository.findByQuizAttemptId(attemptId);
        Map<Long, AttemptAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a1, a2) -> a1));

        List<QuestionResultDto> questionResults = new ArrayList<>();
        for (Question question : quiz.getQuestions()) {
            AttemptAnswer ans = answerMap.get(question.getId());

            List<Long> selectedOptionIds = Collections.emptyList();
            if (ans != null && ans.getSelectedOptionIds() != null && !ans.getSelectedOptionIds().isBlank()) {
                selectedOptionIds = Arrays.stream(ans.getSelectedOptionIds().split(","))
                        .filter(s -> !s.isBlank())
                        .map(Long::valueOf)
                        .collect(Collectors.toList());
            }

            Set<Long> correctOptionIds = question.getOptions().stream()
                    .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                    .map(Option::getId)
                    .collect(Collectors.toSet());

            List<OptionDto> optionDtos = question.getOptions().stream()
                    .map(o -> new OptionDto(o.getId(), o.getOptionText(), o.getIsCorrect()))
                    .collect(Collectors.toList());

            boolean isCorrect = ans != null && Boolean.TRUE.equals(ans.getIsCorrect());
            int marksAwarded = ans != null ? ans.getMarksAwarded() : 0;

            questionResults.add(new QuestionResultDto(
                    question.getId(),
                    question.getQuestionText(),
                    question.getQuestionType(),
                    question.getMarks(),
                    marksAwarded,
                    isCorrect,
                    question.getExplanation(),
                    optionDtos,
                    selectedOptionIds,
                    new ArrayList<>(correctOptionIds)
            ));
        }

        double percentage = attempt.getTotalMarks() > 0
                ? ((double) attempt.getScore() / attempt.getTotalMarks()) * 100.0
                : 0.0;

        return new QuizResultResponseDto(
                attempt.getId(),
                quiz.getId(),
                quiz.getTitle(),
                attempt.getUser().getFullName() != null ? attempt.getUser().getFullName() : attempt.getUser().getUsername(),
                attempt.getScore(),
                attempt.getTotalMarks(),
                attempt.getTotalQuestions(),
                attempt.getCorrectAnswers(),
                Math.round(percentage * 100.0) / 100.0,
                attempt.getPassed(),
                quiz.getPassingScore(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getTimeTakenSeconds(),
                attempt.getStatus(),
                questionResults
        );
    }

    @Transactional(readOnly = true)
    public List<AttemptSummaryDto> getUserAttempts(Long userId) {
        return attemptRepository.findByUserIdOrderByStartedAtDesc(userId).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttemptSummaryDto> getQuizAttempts(Long quizId) {
        return attemptRepository.findByQuizIdOrderByStartedAtDesc(quizId).stream()
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AttemptSummaryDto> getAllAttempts() {
        return attemptRepository.findAll().stream()
                .sorted(Comparator.comparing(QuizAttempt::getStartedAt).reversed())
                .map(this::mapToSummaryDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getAnalyticsSummary() {
        long totalQuizzes = quizRepository.count();
        long totalCategories = categoryRepository.count();
        long totalQuestions = questionRepository.count();
        long totalStudents = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_STUDENT)
                .count();
        long totalAttempts = attemptRepository.count();

        long passedAttempts = attemptRepository.findAll().stream()
                .filter(a -> Boolean.TRUE.equals(a.getPassed()))
                .count();

        double passRate = totalAttempts > 0
                ? Math.round(((double) passedAttempts / totalAttempts) * 10000.0) / 100.0
                : 0.0;

        return new AnalyticsSummaryDto(
                totalQuizzes,
                totalCategories,
                totalQuestions,
                totalStudents,
                totalAttempts,
                passRate
        );
    }

    @Transactional
    public int autoExpireTimedOutAttempts() {
        LocalDateTime now = LocalDateTime.now();
        List<QuizAttempt> inProgressList = attemptRepository.findAll().stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        int expiredCount = 0;
        for (QuizAttempt attempt : inProgressList) {
            long limitSeconds = (attempt.getQuiz().getTimeLimitMinutes() * 60L) + 120L; // 2 min grace
            long elapsed = Duration.between(attempt.getStartedAt(), now).getSeconds();
            if (elapsed > limitSeconds) {
                attempt.setStatus(AttemptStatus.TIMED_OUT);
                attempt.setCompletedAt(now);
                attempt.setTimeTakenSeconds(elapsed);
                attempt.setScore(0);
                attempt.setPassed(false);
                attemptRepository.save(attempt);
                expiredCount++;
            }
        }
        return expiredCount;
    }

    private AttemptSummaryDto mapToSummaryDto(QuizAttempt a) {
        double percentage = a.getTotalMarks() > 0
                ? ((double) a.getScore() / a.getTotalMarks()) * 100.0
                : 0.0;

        String categoryName = a.getQuiz() != null && a.getQuiz().getCategory() != null
                ? a.getQuiz().getCategory().getName()
                : "General";

        return new AttemptSummaryDto(
                a.getId(),
                a.getQuiz().getId(),
                a.getQuiz().getTitle(),
                categoryName,
                a.getUser().getId(),
                a.getUser().getUsername(),
                a.getUser().getFullName(),
                a.getScore(),
                a.getTotalMarks(),
                Math.round(percentage * 100.0) / 100.0,
                a.getPassed(),
                a.getQuiz().getPassingScore(),
                a.getStartedAt(),
                a.getCompletedAt(),
                a.getTimeTakenSeconds(),
                a.getStatus()
        );
    }
}
