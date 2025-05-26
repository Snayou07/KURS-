// com/example/olx/domain/repository/UserRepository.java
package com.example.olx.domain.repository;

import com.example.olx.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(String id);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    void deleteById(String id);
}


