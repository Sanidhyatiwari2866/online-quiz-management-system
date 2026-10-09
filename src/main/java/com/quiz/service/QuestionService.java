package com.quiz.service;

import com.quiz.dto.OptionDto;
import com.quiz.dto.QuestionDto;
import com.quiz.exception.BadRequestException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Option;
import com.quiz.model.Question;
import com.quiz.model.Quiz;
import com.quiz.repository.OptionRepository;
import com.quiz.repository.QuestionRepository;
import com.quiz.repository.QuizRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final OptionRepository optionRepository;

    public QuestionService(QuestionRepository questionRepository,
                           QuizRepository quizRepository,
                           OptionRepository optionRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.optionRepository = optionRepository;
    }

    @Transactional(readOnly = true)
    public QuestionDto getQuestionById(Long id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + id));

        List<OptionDto> options = question.getOptions().stream()
                .map(o -> new OptionDto(o.getId(), o.getOptionText(), o.getIsCorrect()))
                .collect(Collectors.toList());

        return new QuestionDto(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getMarks(),
                question.getExplanation(),
                question.getQuiz().getId(),
                options
        );
    }

    @Transactional
    public QuestionDto addQuestionToQuiz(Long quizId, QuestionDto dto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
            throw new BadRequestException("A question must have at least one option");
        }

        boolean hasCorrect = dto.getOptions().stream().anyMatch(o -> Boolean.TRUE.equals(o.getIsCorrect()));
        if (!hasCorrect) {
            throw new BadRequestException("At least one option must be marked as correct");
        }

        Question question = new Question();
        question.setQuestionText(dto.getQuestionText().trim());
        question.setQuestionType(dto.getQuestionType());
        question.setMarks(dto.getMarks() != null && dto.getMarks() > 0 ? dto.getMarks() : 1);
        question.setExplanation(dto.getExplanation());
        question.setQuiz(quiz);

        for (OptionDto optDto : dto.getOptions()) {
            Option option = new Option(optDto.getOptionText().trim(), optDto.getIsCorrect());
            question.addOption(option);
        }

        Question savedQuestion = questionRepository.save(question);

        // Recalculate Quiz Total Marks
        quiz.getQuestions().add(savedQuestion);
        quiz.recalculateTotalMarks();
        quizRepository.save(quiz);

        return mapToDto(savedQuestion);
    }

    @Transactional
    public QuestionDto updateQuestion(Long questionId, QuestionDto dto) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (dto.getOptions() == null || dto.getOptions().isEmpty()) {
            throw new BadRequestException("A question must have at least one option");
        }

        boolean hasCorrect = dto.getOptions().stream().anyMatch(o -> Boolean.TRUE.equals(o.getIsCorrect()));
        if (!hasCorrect) {
            throw new BadRequestException("At least one option must be marked as correct");
        }

        question.setQuestionText(dto.getQuestionText().trim());
        question.setQuestionType(dto.getQuestionType());
        question.setMarks(dto.getMarks() != null && dto.getMarks() > 0 ? dto.getMarks() : 1);
        question.setExplanation(dto.getExplanation());

        // Replace options
        question.getOptions().clear();
        for (OptionDto optDto : dto.getOptions()) {
            Option option = new Option(optDto.getOptionText().trim(), optDto.getIsCorrect());
            question.addOption(option);
        }

        Question updatedQuestion = questionRepository.save(question);

        // Recalculate Quiz Total Marks
        Quiz quiz = question.getQuiz();
        quiz.recalculateTotalMarks();
        quizRepository.save(quiz);

        return mapToDto(updatedQuestion);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        Quiz quiz = question.getQuiz();
        quiz.getQuestions().remove(question);
        questionRepository.delete(question);

        quiz.recalculateTotalMarks();
        quizRepository.save(quiz);
    }

    private QuestionDto mapToDto(Question q) {
        List<OptionDto> options = q.getOptions().stream()
                .map(o -> new OptionDto(o.getId(), o.getOptionText(), o.getIsCorrect()))
                .collect(Collectors.toList());

        return new QuestionDto(
                q.getId(),
                q.getQuestionText(),
                q.getQuestionType(),
                q.getMarks(),
                q.getExplanation(),
                q.getQuiz() != null ? q.getQuiz().getId() : null,
                options
        );
    }
}
