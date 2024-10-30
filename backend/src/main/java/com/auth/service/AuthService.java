package com.auth.service;

import com.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {
    private final Map<String, User> users = new HashMap<>();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionManager sessionManager;

    public void register(User user) {
        if (users.containsKey(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        // Encrypt password before storing
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        users.put(user.getUsername(), user);
    }

    public Optional<User> authenticate(String username, String password) {
        User user = users.get(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public void logout(String username) {
        sessionManager.invalidateSession(username);
    }
}
