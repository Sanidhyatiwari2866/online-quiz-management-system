# 🎓 Online Quiz Management System
### Enterprise Java-Based Online Quiz Platform

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)
[![JWT](https://img.shields.io/badge/JWT-JJWT_0.12-000000?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)
[![Database](https://img.shields.io/badge/Database-H2%20%7C%20MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://mysql.com/)
[![Tests](https://img.shields.io/badge/JUnit5-14%20Passed-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)

> **Review 1: Code Completion** | Full-Stack Online Quiz & Assessment Platform developed for College Capstone Project at Galgotias University.

A full-featured, secure, and responsive web-based quiz application for computer science and programming assessments. Includes timed test sessions with auto-submission, dynamic scoring strategies (Standard and Negative Marking), multi-role RBAC, global leaderboard podium, printable certificates, background concurrency monitors, and dual persistence options (H2 in-memory and MySQL).

---

## 🌟 Features Implemented

| Feature | Status |
|---|---|
| User Registration & Login (JWT) | ✅ Complete |
| Role-Based Access Control (Admin / Teacher / Student) | ✅ Complete |
| BCrypt Password Hashing | ✅ Complete |
| Quiz Catalog with Search & Category Filter | ✅ Complete |
| Quiz Creation, Edit, Delete (Admin/Teacher) | ✅ Complete |
| Admin Quiz Approval / Rejection Workflow | ✅ Complete |
| Question Builder (Single, Multiple, True/False) | ✅ Complete |
| Timed Quiz with Live Countdown Timer | ✅ Complete |
| Auto-Submit on Timer Expiry | ✅ Complete |
| Accurate Score Calculation | ✅ Complete |
| Per-Question Result Breakdown with Explanations | ✅ Complete |
| Quiz Attempt History (Student) | ✅ Complete |
| Global Leaderboard with Podium | ✅ Complete |
| Feedback / Rating System | ✅ Complete |
| Admin Dashboard (KPIs, Users, Alerts, Settings) | ✅ Complete |
| Certificate of Achievement (Printable) | ✅ Complete |
| Background Thread — Timeout Monitor | ✅ Complete |
| JDBC DAO Layer (UserDAO, QuizDAO, ResultDAO, etc.) | ✅ Complete |
| OOP Strategy Pattern (Scoring) | ✅ Complete |
| JUnit Integration Tests | ✅ Complete |

---

## 👥 User Roles & Credentials

Pre-seeded demo data is loaded automatically on first startup:

| Role | Username | Password | Access |
|---|---|---|---|
| **Admin** | `admin` | `admin123` | Full system control, quiz approval, user management |
| **Teacher** | `teacher` | `teacher123` | Create quizzes, manage questions, view analytics |
| **Student** | `student` | `student123` | Take quizzes, view scores, leaderboard |
| **Student** | `rohit` | `student123` | Additional demo student account |

*(Register a new account anytime from the web UI — the Quick Login buttons fill credentials automatically)*

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FRONTEND (HTML5 + Bootstrap 5 + Vanilla JS)      │
│   auth.js │ student.js │ admin.js │ api.js │ app.js                  │
└─────────────────────────────┬───────────────────────────────────────┘
                              │  HTTP REST (JSON)
                              │  JWT Bearer Token
┌─────────────────────────────▼───────────────────────────────────────┐
│              SPRING BOOT 3.3 APPLICATION (Port 8080)                │
│                                                                     │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │  Security Layer: JwtAuthenticationFilter + SecurityConfig   │    │
│  └─────────────────────────────────────────────────────────────┘    │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Controllers: Auth │ Quiz │ Question │ Attempt │ Admin │      │   │
│  │               Category │ Leaderboard │ Feedback               │   │
│  └────────────────────────────┬─────────────────────────────────┘   │
│  ┌─────────────────────────────▼───────────────────────────────┐    │
│  │  Services: AuthService │ QuizService │ QuizAttemptService    │    │
│  │            QuestionService │ FeedbackService │ CategoryService│    │
│  │            ScoringStrategyFactory (Strategy Pattern)         │    │
│  │            QuizAttemptTimeoutMonitor (Background Thread)     │    │
│  └────────┬────────────────────────────────────┬───────────────┘    │
│           │ Spring Data JPA                    │ Direct JDBC         │
│  ┌────────▼──────────┐              ┌──────────▼──────────────┐     │
│  │  JPA Repositories │              │    DAO Layer (JDBC)      │     │
│  │  UserRepository   │              │  UserDAOImpl            │     │
│  │  QuizRepository   │              │  QuizDAOImpl            │     │
│  │  QuestionRepo     │              │  ResultDAOImpl          │     │
│  │  AttemptRepo      │              │  QuestionDAOImpl        │     │
│  │  FeedbackRepo     │              │  FeedbackDAOImpl        │     │
│  └────────┬──────────┘              └──────────┬──────────────┘     │
│           └──────────────┬──────────────────────┘                   │
│                 ┌────────▼────────┐                                  │
│                 │  H2 / MySQL DB  │                                  │
│                 └─────────────────┘                                  │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Entity Relationships

```
[User] ──────────── (creates) ──────────── [Quiz]
   │                                          │
   │ (attempts)                               │ (has)
   │                                          │
[QuizAttempt] ──────────────────────── [Question]
   │                                          │
   │ (stores)                                 │ (has)
   │                                          │
[AttemptAnswer]                           [Option]

[User] ──── (gives) ──── [Feedback] ──── (for) ──── [Quiz]
[Quiz] ──── (belongs to) ──── [Category]
```

### Entity Fields

| Entity | Key Fields |
|---|---|
| **User** | id, username, email, password (BCrypt), fullName, role, enabled |
| **Category** | id, name, description |
| **Quiz** | id, title, description, timeLimitMinutes, totalMarks, passingScore, randomizeQuestions, status, rejectionReason, category_id, created_by_user_id |
| **Question** | id, questionText, questionType (SINGLE/MULTIPLE/TRUE_FALSE), marks, explanation, quiz_id |
| **Option** | id, optionText, isCorrect, question_id |
| **QuizAttempt** | id, user_id, quiz_id, score, totalMarks, totalQuestions, correctAnswers, startedAt, completedAt, timeTakenSeconds, status, passed |
| **AttemptAnswer** | id, attempt_id, question_id, selectedOptionIds (CSV), isCorrect, marksAwarded |
| **Feedback** | id, user_id, quiz_id, rating (1-5), comment |

---

## ☕ Java Concepts Demonstrated

| Concept | Implementation Location |
|---|---|
| **Encapsulation** | All model classes — private fields, public getters/setters |
| **Inheritance** | `User`, `Quiz`, `Category`, `Question`, `Feedback` extend `BaseEntity` |
| **Abstraction** | `BaseEntity` abstract class (common id, createdAt, updatedAt) |
| **Polymorphism** | `ScoringStrategy` interface → `StandardScoringStrategy`, `NegativeMarkingScoringStrategy` |
| **Interfaces** | `ScoringStrategy`, `UserDAO`, `QuizDAO`, `ResultDAO`, `QuestionDAO`, `FeedbackDAO` |
| **Generics & Collections** | `List<Quiz>`, `Map<String, ScoringStrategy>`, `Set<Long>`, `HashMap` |
| **Exception Handling** | `GlobalExceptionHandler`, custom `ResourceNotFoundException`, `BadRequestException`, `UnauthorizedException` |
| **Multithreading** | `QuizAttemptTimeoutMonitor` — `ScheduledExecutorService`, `ThreadFactory`, `AtomicInteger`, `synchronized` |
| **JDBC + PreparedStatement** | All `*DAOImpl` classes — SQL injection prevention, try-with-resources |
| **Design Pattern** | Strategy Pattern (`ScoringStrategyFactory`), DAO Pattern, MVC Pattern |
| **Lambdas & Streams** | Used throughout service layer — `filter`, `map`, `collect`, `sorted` |
| **Input Validation** | `@Valid` on DTOs, `@NotBlank`, `@NotNull`, `@Min`, `@Max` annotations |

---

## 🚀 How to Run

### Prerequisites
- **Java 17 or Java 21** — verify with `java -version`
- **Maven 3.8+** — OR use IntelliJ IDEA's built-in Maven

### Option 1: Run with H2 In-Memory Database (Zero Setup — Recommended for Demo)

```bash
# From the project root directory:
mvn clean spring-boot:run

# OR if Maven is not on PATH, use IntelliJ IDEA's Run button (main class: OnlineQuizApplication)
```

Then open your browser:
- **Application UI**: http://localhost:8080
- **H2 Database Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:quizdb`
  - Username: `sa`
  - Password: *(leave blank)*

> **Note:** H2 is in-memory — data resets every time the app restarts. This is expected for the demo.

### Option 2: Run with MySQL (Persistent Data)

1. Start MySQL server (already installed on your machine).
2. Update `src/main/resources/application-mysql.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/quizdb?createDatabaseIfNotExist=true
   spring.datasource.username=root
   spring.datasource.password=YOUR_PASSWORD_HERE
   ```
3. Switch profile in `application.properties`:
   ```properties
   spring.profiles.active=mysql
   ```
4. Run:
   ```bash
   mvn clean spring-boot:run
   ```

---

## 📡 REST API Reference

### Authentication
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Login, receive JWT token |
| GET | `/api/auth/me` | Authenticated | Get current user profile |

### Categories
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/categories` | Public | List all categories |
| POST | `/api/categories` | Admin/Teacher | Create category |
| PUT | `/api/categories/{id}` | Admin/Teacher | Update category |
| DELETE | `/api/categories/{id}` | Admin/Teacher | Delete category |

### Quizzes
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/quizzes` | Public | Browse approved quizzes |
| GET | `/api/quizzes/pending` | Admin | Quizzes pending approval |
| POST | `/api/quizzes` | Admin/Teacher | Create quiz |
| PUT | `/api/quizzes/{id}/approve` | Admin | Approve quiz |
| PUT | `/api/quizzes/{id}/reject` | Admin | Reject quiz with reason |
| DELETE | `/api/quizzes/{id}` | Admin/Teacher | Delete quiz |
| GET | `/api/quizzes/{id}/questions` | Auth | Get questions (answers hidden for students) |
| POST | `/api/quizzes/{id}/questions` | Admin/Teacher | Add question |

### Quiz Attempts
| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/attempts/start/{quizId}` | Student | Start timed quiz |
| POST | `/api/attempts/submit` | Student | Submit answers |
| GET | `/api/attempts/my` | Student | My attempt history |
| GET | `/api/attempts/all` | Admin/Teacher | All student attempts |
| GET | `/api/attempts/analytics` | Admin/Teacher | KPI dashboard data |

### Leaderboard & Feedback
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/leaderboard` | Public | Global top performers |
| GET | `/api/leaderboard/quiz/{id}` | Public | Per-quiz leaderboard |
| POST | `/api/feedback` | Student | Submit quiz rating |
| GET | `/api/feedback/quiz/{id}` | Public | Quiz reviews |

### Admin Management
| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/admin/users` | Admin | List all users |
| PUT | `/api/admin/users/{id}/role` | Admin | Change user role |
| PUT | `/api/admin/users/{id}/status` | Admin | Enable/disable user |
| DELETE | `/api/admin/users/{id}` | Admin | Delete user |
| GET | `/api/admin/alerts` | Admin | System health alerts |
| GET/PUT | `/api/admin/settings` | Admin | Platform settings |

---

## 📁 Project Structure

```
online-quiz-system/
├── pom.xml                          # Maven build file (Spring Boot 3.3, JWT, JPA, H2, MySQL)
├── README.md
├── quiz_platform_schema.sql         # MySQL schema reference
├── mvnw.cmd                         # Maven Wrapper (Windows)
└── src/
    ├── main/
    │   ├── java/com/quiz/
    │   │   ├── OnlineQuizApplication.java       # Main entry point
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java          # Spring Security + JWT filter chain
    │   │   │   ├── JwtTokenProvider.java        # JWT generation & validation (JJWT 0.12)
    │   │   │   ├── JwtAuthenticationFilter.java # Per-request JWT verification
    │   │   │   ├── CorsConfig.java              # CORS configuration
    │   │   │   └── DataInitializer.java         # Seeds demo data on startup
    │   │   ├── model/
    │   │   │   ├── BaseEntity.java              # Abstract base (OOP Abstraction + Inheritance)
    │   │   │   ├── User.java / Role.java
    │   │   │   ├── Quiz.java / QuizStatus.java
    │   │   │   ├── Question.java / QuestionType.java
    │   │   │   ├── Option.java
    │   │   │   ├── QuizAttempt.java / AttemptStatus.java
    │   │   │   ├── AttemptAnswer.java
    │   │   │   ├── Category.java
    │   │   │   └── Feedback.java
    │   │   ├── dto/                             # 23 Data Transfer Objects
    │   │   │   └── (AuthRequest/Response, QuizRequest/Response, ...)
    │   │   ├── repository/                      # Spring Data JPA repositories (8 interfaces)
    │   │   ├── dao/                             # JDBC DAO layer (PreparedStatement, try-with-resources)
    │   │   │   ├── UserDAO.java + UserDAOImpl.java
    │   │   │   ├── QuizDAO.java + QuizDAOImpl.java
    │   │   │   ├── ResultDAO.java + ResultDAOImpl.java
    │   │   │   ├── QuestionDAO.java + QuestionDAOImpl.java
    │   │   │   └── FeedbackDAO.java + FeedbackDAOImpl.java
    │   │   ├── service/
    │   │   │   ├── AuthService.java             # Registration, login, JWT
    │   │   │   ├── QuizService.java             # Quiz CRUD, approval workflow
    │   │   │   ├── QuizAttemptService.java      # Attempt lifecycle, scoring, analytics
    │   │   │   ├── QuestionService.java         # Question CRUD
    │   │   │   ├── CategoryService.java
    │   │   │   ├── FeedbackService.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── scoring/                     # OOP Strategy Pattern
    │   │   │   │   ├── ScoringStrategy.java     # Interface (Abstraction + Polymorphism)
    │   │   │   │   ├── StandardScoringStrategy.java
    │   │   │   │   ├── NegativeMarkingScoringStrategy.java
    │   │   │   │   ├── ScoringStrategyFactory.java
    │   │   │   │   └── ScoreResult.java
    │   │   │   └── concurrent/
    │   │   │       └── QuizAttemptTimeoutMonitor.java  # Background daemon thread
    │   │   ├── controller/                      # 8 REST controllers
    │   │   └── exception/
    │   │       ├── GlobalExceptionHandler.java  # Centralized error handling
    │   │       ├── ResourceNotFoundException.java
    │   │       ├── BadRequestException.java
    │   │       └── UnauthorizedException.java
    │   └── resources/
    │       ├── application.properties           # Active profile, JWT config
    │       ├── application-h2.properties        # H2 dev/demo config
    │       ├── application-mysql.properties     # MySQL production config
    │       └── static/
    │           ├── index.html                   # Single-page application (1024 lines)
    │           ├── css/styles.css
    │           └── js/
    │               ├── api.js                   # REST client layer
    │               ├── auth.js                  # Login/Register/Logout
    │               ├── student.js               # Quiz taking, leaderboard
    │               ├── admin.js                 # Management portal
    │               └── app.js                   # View router
    └── test/
        └── java/com/quiz/
            └── OnlineQuizApplicationTests.java  # JUnit 5 integration tests (14 tests)
```

---

## ✅ Testing Checklist

| Test | Method | Expected |
|---|---|---|
| App starts and serves UI | Open http://localhost:8080 | Home page loads |
| Login as Admin | Username: admin / admin123 | JWT received, Admin tab visible |
| Login as Student | Username: student / student123 | Quiz catalog visible |
| Browse Quiz Catalog | Click quizzes | 2 approved quizzes shown |
| Take a Quiz | Click "Start Quiz" (must login) | Timer counts down |
| Submit Quiz | Click "Submit Quiz" | Score & breakdown shown |
| View Leaderboard | Click Leaderboard tab | Top performers with podium |
| Admin Approval | Login as admin → Management Portal | Pending quiz awaiting approval |
| Create Quiz | Admin → Create New Quiz | Quiz created in PENDING state |
| Add Question | Admin → Quiz → Add Question | Question with options added |
| Invalid Login | Wrong password | "Invalid username or password" |
| H2 Console | http://localhost:8080/h2-console | Database tables visible |

---

## ⚠️ Known Limitations

1. **H2 is in-memory only** — data is lost on restart. Switch to MySQL for persistence.
2. **No email verification** — registration is immediate.
3. **Certificate is browser-print only** — no PDF export library integrated.
4. **System settings** are in-memory (AdminController field) — not persisted to DB.
5. **Leaderboard quiz filter** (dropdown) uses pre-loaded category list — does not yet dynamically load all quizzes.

---

## 🔮 Future Scope

- MySQL persistent deployment on cloud (AWS/GCP)
- Email notifications for quiz approval and results
- PDF certificate export
- Question bank and quiz import/export (CSV/JSON)
- Timer synchronization via WebSocket for multi-tab protection
- Analytics charts using Chart.js
- Dark mode toggle

---

## 👨‍💻 Project Info

- **Student**: Galgotias University, B.Tech CSE
- **Subject**: Java Programming (College Project)
- **Review**: Review 1 — Code Completion
- **Tech Stack**: Java 17/21, Spring Boot 3.3, Spring Security 6, JWT (JJWT 0.12), Spring Data JPA, Hibernate, JDBC, H2/MySQL, Bootstrap 5, Vanilla JS
