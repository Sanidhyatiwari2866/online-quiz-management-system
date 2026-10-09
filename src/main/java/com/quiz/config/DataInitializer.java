package com.quiz.config;

import com.quiz.model.*;
import com.quiz.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           QuizAttemptRepository attemptRepository,
                           AttemptAnswerRepository attemptAnswerRepository,
                           FeedbackRepository feedbackRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already initialized
        }

        System.out.println(">>> Initializing Online Quiz System sample data...");

        // 1. Seed Users
        User admin = new User("admin", "admin@quizsystem.com", passwordEncoder.encode("admin123"), "System Administrator", Role.ROLE_ADMIN);
        User teacher = new User("teacher", "teacher@quizsystem.com", passwordEncoder.encode("teacher123"), "Prof. Robert Smith", Role.ROLE_TEACHER);
        User student = new User("student", "student@quizsystem.com", passwordEncoder.encode("student123"), "Alice Johnson", Role.ROLE_STUDENT);
        User student2 = new User("rohit", "rohit@galgotias.edu", passwordEncoder.encode("student123"), "Rohit Sharma", Role.ROLE_STUDENT);

        userRepository.save(admin);
        userRepository.save(teacher);
        userRepository.save(student);
        userRepository.save(student2);

        // 2. Seed Categories
        Category javaCategory = new Category("Java Programming", "Core Java, OOPs, Collections, Concurrency, and JVM concepts.");
        Category springCategory = new Category("Spring Boot Framework", "Spring Boot 3, Spring Data JPA, REST APIs, and Security.");
        Category webCategory = new Category("Web Technologies & SQL", "Frontend basics, HTTP protocol, SQL databases, and query optimization.");

        categoryRepository.save(javaCategory);
        categoryRepository.save(springCategory);
        categoryRepository.save(webCategory);

        // 3. Seed Quiz 1: Java Core Foundations (APPROVED)
        Quiz javaQuiz = new Quiz("Java Core Foundations", "Test your core Java knowledge on data types, immutability, collections, and memory management.", 10, 60, true, javaCategory);
        javaQuiz.setCreator(teacher);
        javaQuiz.setStatus(QuizStatus.APPROVED);
        quizRepository.save(javaQuiz);

        // Question 1
        Question q1 = new Question("Which of the following is NOT a Java primitive data type?", QuestionType.SINGLE_CHOICE, 2, "String is a class in Java (java.lang.String), whereas int, boolean, and double are primitives.", javaQuiz);
        q1.addOption(new Option("int", false));
        q1.addOption(new Option("boolean", false));
        q1.addOption(new Option("String", true));
        q1.addOption(new Option("double", false));
        questionRepository.save(q1);

        // Question 2
        Question q2 = new Question("In Java, String objects are immutable.", QuestionType.TRUE_FALSE, 1, "True. Once a String object is created in memory, its value cannot be modified. Any modification creates a new String instance.", javaQuiz);
        q2.addOption(new Option("True", true));
        q2.addOption(new Option("False", false));
        questionRepository.save(q2);

        // Question 3
        Question q3 = new Question("Which of the following interfaces extend the java.util.Collection interface?", QuestionType.MULTIPLE_CHOICE, 3, "List and Set extend Collection. Map is part of the Collections Framework but has its own independent hierarchy.", javaQuiz);
        q3.addOption(new Option("java.util.List", true));
        q3.addOption(new Option("java.util.Set", true));
        q3.addOption(new Option("java.util.Map", false));
        q3.addOption(new Option("java.lang.Thread", false));
        questionRepository.save(q3);

        // Question 4
        Question q4 = new Question("Which garbage collector is enabled by default in Java 9 through modern LTS releases?", QuestionType.SINGLE_CHOICE, 2, "G1 (Garbage-First) GC was made the default collector starting with Java 9.", javaQuiz);
        q4.addOption(new Option("Serial Garbage Collector", false));
        q4.addOption(new Option("Parallel Garbage Collector", false));
        q4.addOption(new Option("Garbage-First (G1) Garbage Collector", true));
        q4.addOption(new Option("Z Garbage Collector (ZGC)", false));
        questionRepository.save(q4);

        // Question 5
        Question q5 = new Question("A single Java class can implement multiple interfaces.", QuestionType.TRUE_FALSE, 1, "True. Java supports multiple interface implementation using the 'implements' keyword.", javaQuiz);
        q5.addOption(new Option("True", true));
        q5.addOption(new Option("False", false));
        questionRepository.save(q5);

        javaQuiz.getQuestions().add(q1);
        javaQuiz.getQuestions().add(q2);
        javaQuiz.getQuestions().add(q3);
        javaQuiz.getQuestions().add(q4);
        javaQuiz.getQuestions().add(q5);
        javaQuiz.recalculateTotalMarks();
        quizRepository.save(javaQuiz);

        // 4. Seed Quiz 2: Spring Boot 3 & REST Architecture (APPROVED)
        Quiz springQuiz = new Quiz("Spring Boot 3 & REST Architecture", "Demonstrate your understanding of Spring Boot auto-configuration, dependency injection, and RESTful web services.", 15, 60, false, springCategory);
        springQuiz.setCreator(teacher);
        springQuiz.setStatus(QuizStatus.APPROVED);
        quizRepository.save(springQuiz);

        Question sq1 = new Question("What is the primary purpose of the @SpringBootApplication annotation?", QuestionType.SINGLE_CHOICE, 2, "@SpringBootApplication combines @Configuration, @EnableAutoConfiguration, and @ComponentScan with default attributes.", springQuiz);
        sq1.addOption(new Option("It configures Spring Security with OAuth2 by default", false));
        sq1.addOption(new Option("It aggregates @Configuration, @EnableAutoConfiguration, and @ComponentScan", true));
        sq1.addOption(new Option("It instructs Hibernate to automatically create DB schemas", false));
        sq1.addOption(new Option("It runs the application inside an embedded Docker container", false));
        questionRepository.save(sq1);

        Question sq2 = new Question("Which HTTP status code is standard for successful resource creation via POST?", QuestionType.SINGLE_CHOICE, 2, "HTTP 201 Created indicates that the request has succeeded and led to the creation of a resource.", springQuiz);
        sq2.addOption(new Option("200 OK", false));
        sq2.addOption(new Option("201 Created", true));
        sq2.addOption(new Option("204 No Content", false));
        sq2.addOption(new Option("301 Moved Permanently", false));
        questionRepository.save(sq2);

        Question sq3 = new Question("Spring Boot web applications require an externally installed Apache Tomcat instance to run.", QuestionType.TRUE_FALSE, 1, "False. Spring Boot includes an embedded server (Tomcat by default) packaged inside the executable JAR.", springQuiz);
        sq3.addOption(new Option("True", false));
        sq3.addOption(new Option("False", true));
        questionRepository.save(sq3);

        Question sq4 = new Question("Which annotations are Spring stereotype annotations used to mark beans?", QuestionType.MULTIPLE_CHOICE, 3, "@Component, @Service, and @Repository are stereotype annotations. @Entity is a JPA annotation.", springQuiz);
        sq4.addOption(new Option("@Component", true));
        sq4.addOption(new Option("@Service", true));
        sq4.addOption(new Option("@Repository", true));
        sq4.addOption(new Option("@Entity", false));
        questionRepository.save(sq4);

        springQuiz.getQuestions().add(sq1);
        springQuiz.getQuestions().add(sq2);
        springQuiz.getQuestions().add(sq3);
        springQuiz.getQuestions().add(sq4);
        springQuiz.recalculateTotalMarks();
        quizRepository.save(springQuiz);

        // 5. Seed Quiz 3: Java Concurrency & Multithreading (PENDING_APPROVAL - For Admin approval demonstration!)
        Quiz concurrencyQuiz = new Quiz("Java Concurrency & Multithreading Mastery", "Advanced thread synchronization, Executors, locks, and atomic variables in Java.", 12, 70, false, javaCategory);
        concurrencyQuiz.setCreator(teacher);
        concurrencyQuiz.setStatus(QuizStatus.PENDING_APPROVAL);
        quizRepository.save(concurrencyQuiz);

        Question cq1 = new Question("Which package contains the concurrent collections and ExecutorService in Java?", QuestionType.SINGLE_CHOICE, 2, "java.util.concurrent was introduced in Java 5 and holds concurrency utilities.", concurrencyQuiz);
        cq1.addOption(new Option("java.lang.concurrent", false));
        cq1.addOption(new Option("java.util.concurrent", true));
        cq1.addOption(new Option("java.io.threads", false));
        cq1.addOption(new Option("javax.threading", false));
        questionRepository.save(cq1);

        concurrencyQuiz.getQuestions().add(cq1);
        concurrencyQuiz.recalculateTotalMarks();
        quizRepository.save(concurrencyQuiz);

        // 6. Seed Sample Attempt for Leaderboard demonstration
        QuizAttempt sampleAttempt = new QuizAttempt(student, javaQuiz);
        sampleAttempt.setScore(8);
        sampleAttempt.setTotalMarks(9);
        sampleAttempt.setTotalQuestions(5);
        sampleAttempt.setCorrectAnswers(4);
        sampleAttempt.setTimeTakenSeconds(245L);
        sampleAttempt.setStatus(AttemptStatus.COMPLETED);
        sampleAttempt.setPassed(true);
        sampleAttempt.setStartedAt(LocalDateTime.now().minusHours(2));
        sampleAttempt.setCompletedAt(LocalDateTime.now().minusHours(2).plusMinutes(4));
        attemptRepository.save(sampleAttempt);

        QuizAttempt sampleAttempt2 = new QuizAttempt(student2, javaQuiz);
        sampleAttempt2.setScore(9);
        sampleAttempt2.setTotalMarks(9);
        sampleAttempt2.setTotalQuestions(5);
        sampleAttempt2.setCorrectAnswers(5);
        sampleAttempt2.setTimeTakenSeconds(180L);
        sampleAttempt2.setStatus(AttemptStatus.COMPLETED);
        sampleAttempt2.setPassed(true);
        sampleAttempt2.setStartedAt(LocalDateTime.now().minusHours(1));
        sampleAttempt2.setCompletedAt(LocalDateTime.now().minusHours(1).plusMinutes(3));
        attemptRepository.save(sampleAttempt2);

        // 7. Seed Sample Feedback
        Feedback feedback1 = new Feedback(student, javaQuiz, 5, "Excellent questions covering core JVM and String pool concepts! Really helped me prepare.");
        feedbackRepository.save(feedback1);

        Feedback feedback2 = new Feedback(student2, javaQuiz, 4, "Challenging multiple choice questions. The explanations provided after submission are very clear.");
        feedbackRepository.save(feedback2);

        System.out.println(">>> Sample data successfully populated: Admin (admin/admin123), Teacher (teacher/teacher123), Student (student/student123)");
    }
}
