package com.example.olx.application.service.port;

import com.example.olx.domain.exception.UserNotFoundException;
import com.example.olx.domain.model.User;

import java.util.List;

public interface UserService {
    User registerUser(String username, String password, String email);
    User loginUser(String username, String password) throws UserNotFoundException;
    User getUserById(String id) throws UserNotFoundException;
    List<User> getAllUsers();

    void setCurrentUser(User loggedInUser);
}