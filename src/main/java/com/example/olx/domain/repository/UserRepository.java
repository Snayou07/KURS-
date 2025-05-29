package com.example.olx.domain.repository;

import com.example.olx.domain.model.User;
import java.util.List; // Для findAll
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username); // ДОДАНО
    List<User> findAll();                         // ДОДАНО
}