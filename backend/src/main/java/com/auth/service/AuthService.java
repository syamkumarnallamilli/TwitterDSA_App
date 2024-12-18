package com.auth.service;

import com.auth.cache.CacheService;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private CacheService cacheService; // Inject CacheService

    // Register a new user
    public User register(User user) {
        logger.info("Registering user with username: {}", user.getUsername());

        // Validate username (Instagram-style: alphanumeric, underscores, periods)
        if (!user.getUsername().matches("^[a-zA-Z0-9_\\.]+$")) {
            logger.warn("Registration failed: Invalid username format for username: {}", user.getUsername());
            throw new RuntimeException("Username can only contain letters, numbers, underscores, and periods");
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username {} already exists", user.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Validate password
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            logger.warn("Registration failed: Password cannot be empty");
            throw new RuntimeException("Password cannot be empty");
        }

        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

    
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

       
        cacheService.addUserToCache(savedUser);
        logger.info("User {} added to the cache after registration", user.getUsername());

        return savedUser;
    }
    // Authenticate a user with username and password
    public Optional<User> authenticate(String username, String password) {
        logger.info("Authenticating user with username: {}", username);

        // First, check the cache for the user
        User cachedUser = cacheService.getUserByUsername(username);
        if (cachedUser != null) {
            logger.info("User {} found in cache", username);

            if (passwordEncoder.matches(password, cachedUser.getPassword())) {
                logger.info("Authentication successful for user {} via cache", username);
                return Optional.of(cachedUser);
            } else {
                logger.warn("Authentication failed for user {} via cache: Incorrect password", username);
            }
        } else {
            logger.info("User {} not found in cache. Checking database...", username);
        }

        // If not in cache, check the database
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            logger.info("User {} found in database", username);

            if (passwordEncoder.matches(password, userOpt.get().getPassword())) {
                logger.info("Authentication successful for user {} via database", username);

                // Add the user to the cache after successful authentication
                cacheService.addUserToCache(userOpt.get());
                logger.info("User {} added to cache after successful authentication", username);

                return userOpt;
            } else {
                logger.warn("Authentication failed for user {} via database: Incorrect password", username);
            }
        } else {
            logger.warn("User {} not found in database", username);
        }

        return Optional.empty();
    }

    // Logout a user and invalidate their session
    public void logout(String username) {
        logger.info("Logging out user with username: {}", username);
        sessionManager.invalidateSession(username);
        logger.info("User {} logged out and session invalidated", username);
    }

    public Iterable<User> getAllUsers() {
        if (cacheService.isCacheEmpty()) {
            logger.info("Cache is empty. Fetching all users from the database...");
            return userRepository.findAll();
        } else {
            logger.info("Fetching all users from the cache...");
            return cacheService.getAllUsersFromCache();
        }
    } 
  
    

}
