package com.quiz.service;

import com.quiz.dao.FeedbackDAO;
import com.quiz.dao.QuizDAO;
import com.quiz.dto.OptionDto;
import com.quiz.dto.QuestionDto;
import com.quiz.dto.QuizRequestDto;
import com.quiz.dto.QuizResponseDto;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.*;
import com.quiz.repository.CategoryRepository;
import com.quiz.repository.QuestionRepository;
import com.quiz.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizDAO quizDAO;
    private final FeedbackDAO feedbackDAO;

    public QuizService(QuizRepository quizRepository,
                       CategoryRepository categoryRepository,
                       QuestionRepository questionRepository,
                       @Qualifier("quizJdbcDAO") QuizDAO quizDAO,
                       @Qualifier("feedbackJdbcDAO") FeedbackDAO feedbackDAO) {
        this.quizRepository = quizRepository;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizDAO = quizDAO;
        this.feedbackDAO = feedbackDAO;
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDto> getAllQuizzes(Long categoryId, String keyword, boolean includeAll) {
        List<Quiz> quizzes;
        if (includeAll) {
            if (StringUtils.hasText(keyword)) {
                quizzes = quizRepository.searchByKeyword(keyword.trim());
                if (categoryId != null) {
                    quizzes = quizzes.stream()
                            .filter(q -> q.getCategory() != null && q.getCategory().getId().equals(categoryId))
                            .collect(Collectors.toList());
                }
            } else if (categoryId != null) {
                quizzes = quizRepository.findByCategoryId(categoryId);
            } else {
                quizzes = quizRepository.findAll();
            }
        } else {
            // For students / public catalog: show ONLY APPROVED quizzes!
            if (StringUtils.hasText(keyword)) {
                quizzes = quizRepository.searchApprovedByKeyword(QuizStatus.APPROVED, keyword.trim());
                if (categoryId != null) {
                    quizzes = quizzes.stream()
                            .filter(q -> q.getCategory() != null && q.getCategory().getId().equals(categoryId))
                            .collect(Collectors.toList());
                }
            } else if (categoryId != null) {
                quizzes = quizRepository.findByStatusAndCategoryId(QuizStatus.APPROVED, categoryId);
            } else {
                quizzes = quizRepository.findByStatus(QuizStatus.APPROVED);
            }
        }

        return quizzes.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<QuizResponseDto> getPendingQuizzes() {
        return quizRepository.findByStatus(QuizStatus.PENDING_APPROVAL).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuizResponseDto getQuizById(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        return mapToResponseDto(quiz);
    }

    @Transactional
    public QuizResponseDto createQuiz(QuizRequestDto dto, User creator) {
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        Quiz quiz = new Quiz();
        quiz.setTitle(dto.getTitle().trim());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        quiz.setPassingScore(dto.getPassingScore());
        quiz.setRandomizeQuestions(dto.getRandomizeQuestions() != null ? dto.getRandomizeQuestions() : false);
        quiz.setCategory(category);
        quiz.setCreator(creator);
        quiz.setTotalMarks(0);

        // If created by Admin, auto-approve; if created by Teacher/Creator, set to PENDING_APPROVAL
        if (creator != null && creator.getRole() == Role.ROLE_ADMIN) {
            quiz.setStatus(QuizStatus.APPROVED);
        } else {
            quiz.setStatus(QuizStatus.PENDING_APPROVAL);
        }

        Quiz saved = quizRepository.save(quiz);
        return mapToResponseDto(saved);
    }

    @Transactional
    public QuizResponseDto updateQuiz(Long id, QuizRequestDto dto) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId()));

        quiz.setTitle(dto.getTitle().trim());
        quiz.setDescription(dto.getDescription());
        quiz.setTimeLimitMinutes(dto.getTimeLimitMinutes());
        quiz.setPassingScore(dto.getPassingScore());
        quiz.setRandomizeQuestions(dto.getRandomizeQuestions() != null ? dto.getRandomizeQuestions() : false);
        quiz.setCategory(category);

        Quiz updated = quizRepository.save(quiz);
        return mapToResponseDto(updated);
    }

    @Transactional
    public QuizResponseDto approveQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quiz.setStatus(QuizStatus.APPROVED);
        quiz.setRejectionReason(null);
        Quiz saved = quizRepository.save(quiz);
        return mapToResponseDto(saved);
    }

    @Transactional
    public QuizResponseDto rejectQuiz(Long id, String reason) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quiz.setStatus(QuizStatus.REJECTED);
        quiz.setRejectionReason(reason != null ? reason : "Does not meet platform guidelines");
        Quiz saved = quizRepository.save(quiz);
        return mapToResponseDto(saved);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + id));
        quizRepository.delete(quiz);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsForQuiz(Long quizId, boolean isStudentView) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        List<Question> questions = questionRepository.findByQuizIdOrderByIdAsc(quizId);

        if (isStudentView && Boolean.TRUE.equals(quiz.getRandomizeQuestions())) {
            questions = new java.util.ArrayList<>(questions);
            Collections.shuffle(questions);
        }

        return questions.stream()
                .map(q -> mapQuestionToDto(q, isStudentView))
                .collect(Collectors.toList());
    }

    public QuizResponseDto mapToResponseDto(Quiz quiz) {
        int questionCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        String categoryName = quiz.getCategory() != null ? quiz.getCategory().getName() : "Uncategorized";
        Long categoryId = quiz.getCategory() != null ? quiz.getCategory().getId() : null;
        String creatorName = quiz.getCreator() != null ? quiz.getCreator().getUsername() : "System";
        double avgRating = feedbackDAO.getAverageRatingForQuiz(quiz.getId());

        return new QuizResponseDto(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTimeLimitMinutes(),
                quiz.getTotalMarks(),
                quiz.getPassingScore(),
                quiz.getRandomizeQuestions(),
                categoryId,
                categoryName,
                questionCount,
                creatorName,
                quiz.getStatus(),
                quiz.getRejectionReason(),
                avgRating,
                quiz.getCreatedAt()
        );
    }

    public QuestionDto mapQuestionToDto(Question question, boolean isStudentView) {
        List<OptionDto> optionDtos = question.getOptions().stream()
                .map(opt -> new OptionDto(
                        opt.getId(),
                        opt.getOptionText(),
                        isStudentView ? null : opt.getIsCorrect() // Security: Hide answers from student during quiz!
                ))
                .collect(Collectors.toList());

        return new QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getMarks(),
                isStudentView ? null : question.getExplanation(), // Security: Hide explanation from student during quiz!
                question.getQuiz() != null ? question.getQuiz().getId() : null,
                optionDtos
        );
    }
}
