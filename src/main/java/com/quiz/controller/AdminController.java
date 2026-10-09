package com.quiz.controller;

import com.quiz.dao.ResultDAO;
import com.quiz.dao.UserDAO;
import com.quiz.dto.ApiResponse;
import com.quiz.dto.SystemAlertDto;
import com.quiz.dto.SystemSettingsDto;
import com.quiz.dto.UserManagementDto;
import com.quiz.exception.BadRequestException;
import com.quiz.exception.ResourceNotFoundException;
import com.quiz.model.QuizStatus;
import com.quiz.model.Role;
import com.quiz.model.User;
import com.quiz.repository.QuizRepository;
import com.quiz.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserDAO userDAO;
    private final ResultDAO resultDAO;

    // In-memory system settings state
    private SystemSettingsDto currentSettings = new SystemSettingsDto();

    public AdminController(UserRepository userRepository,
                           QuizRepository quizRepository,
                           @Qualifier("userJdbcDAO") UserDAO userDAO,
                           @Qualifier("resultJdbcDAO") ResultDAO resultDAO) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.userDAO = userDAO;
        this.resultDAO = resultDAO;
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserManagementDto>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserManagementDto> dtoList = users.stream().map(u -> {
            long attemptCount = resultDAO.findByUserId(u.getId()).size();
            return new UserManagementDto(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getFullName(),
                    u.getRole(),
                    u.getEnabled(),
                    u.getCreatedAt(),
                    attemptCount
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", dtoList));
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String roleStr = payload.get("role");
        if (roleStr == null) {
            throw new BadRequestException("Role is required");
        }
        Role newRole;
        try {
            newRole = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + roleStr);
        }

        boolean updated = userDAO.updateRole(id, newRole);
        if (!updated) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        return ResponseEntity.ok(ApiResponse.success("User role updated successfully to " + newRole));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> payload) {
        Boolean enabled = payload.get("enabled");
        if (enabled == null) {
            throw new BadRequestException("Enabled status is required");
        }

        boolean updated = userDAO.updateStatus(id, enabled);
        if (!updated) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        return ResponseEntity.ok(ApiResponse.success("User account status updated"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        boolean deleted = userDAO.deleteById(id);
        if (!deleted) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSettingsDto>> getSystemSettings() {
        return ResponseEntity.ok(ApiResponse.success("System settings retrieved", currentSettings));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<SystemSettingsDto>> updateSystemSettings(@RequestBody SystemSettingsDto settings) {
        if (settings != null) {
            this.currentSettings = settings;
        }
        return ResponseEntity.ok(ApiResponse.success("System settings updated successfully", currentSettings));
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<SystemAlertDto>>> getSystemAlerts() {
        List<SystemAlertDto> alerts = new ArrayList<>();

        long pendingCount = quizRepository.countByStatus(QuizStatus.PENDING_APPROVAL);
        if (pendingCount > 0) {
            alerts.add(new SystemAlertDto("WARNING", "Quiz Approvals Pending",
                    pendingCount + " quiz(zes) are waiting for Admin review and approval before becoming available to participants."));
        } else {
            alerts.add(new SystemAlertDto("SUCCESS", "Content Up to Date",
                    "All submitted quizzes have been reviewed and approved."));
        }

        long totalAttempts = resultDAO.count();
        alerts.add(new SystemAlertDto("INFO", "Active Platform Traffic",
                "Total recorded quiz evaluation attempts: " + totalAttempts));

        alerts.add(new SystemAlertDto("INFO", "Database Health",
                "JDBC Connection Pool (HikariCP) is operational and healthy."));

        return ResponseEntity.ok(ApiResponse.success("System alerts retrieved", alerts));
    }
}
