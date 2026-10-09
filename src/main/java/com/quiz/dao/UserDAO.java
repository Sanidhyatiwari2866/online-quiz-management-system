package com.quiz.dao;

import com.quiz.model.Role;
import com.quiz.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) interface for User operations.
 * Demonstrates separation of concerns and interface-based abstraction for JDBC persistence.
 */
public interface UserDAO {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    User save(User user);

    boolean updateRole(Long userId, Role newRole);

    boolean updateStatus(Long userId, boolean enabled);

    boolean deleteById(Long id);

    long count();
}
