package com.quiz.service;

import com.quiz.dao.FeedbackDAO;
import com.quiz.dto.FeedbackDto;
import com.quiz.dto.FeedbackRequestDto;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.Feedback;
import com.quiz.model.Quiz;
import com.quiz.model.User;
import com.quiz.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    private final FeedbackDAO feedbackDAO;
    private final QuizRepository quizRepository;

    public FeedbackService(@Qualifier("feedbackJdbcDAO") FeedbackDAO feedbackDAO,
                           QuizRepository quizRepository) {
        this.feedbackDAO = feedbackDAO;
        this.quizRepository = quizRepository;
    }

    @Transactional
    public FeedbackDto submitFeedback(FeedbackRequestDto request, User user) {
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + request.getQuizId()));

        Feedback feedback = new Feedback(user, quiz, request.getRating(), request.getComment());
        Feedback saved = feedbackDAO.save(feedback);

        return new FeedbackDto(
                saved.getId(),
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                quiz.getId(),
                quiz.getTitle(),
                saved.getRating(),
                saved.getComment(),
                saved.getCreatedAt()
        );
    }

    public List<FeedbackDto> getFeedbackForQuiz(Long quizId) {
        return feedbackDAO.findByQuizId(quizId).stream()
                .map(f -> new FeedbackDto(
                        f.getId(),
                        f.getUser().getId(),
                        f.getUser().getUsername(),
                        f.getUser().getFullName(),
                        f.getQuiz().getId(),
                        f.getQuiz().getTitle(),
                        f.getRating(),
                        f.getComment(),
                        f.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public List<FeedbackDto> getFeedbackForUser(Long userId) {
        return feedbackDAO.findByUserId(userId).stream()
                .map(f -> new FeedbackDto(
                        f.getId(),
                        f.getUser().getId(),
                        f.getUser().getUsername(),
                        f.getUser().getFullName(),
                        f.getQuiz().getId(),
                        f.getQuiz().getTitle(),
                        f.getRating(),
                        f.getComment(),
                        f.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    public double getAverageRating(Long quizId) {
        return feedbackDAO.getAverageRatingForQuiz(quizId);
    }
}
