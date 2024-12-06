// package com.auth.service;

// import com.auth.model.User;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.stereotype.Service;

// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;

// @Service
// public class AuthService {
//     private final Map<String, User> users = new HashMap<>();

//     @Autowired
//     private PasswordEncoder passwordEncoder;

//     @Autowired
//     private SessionManager sessionManager;

//     public void register(User user) {
//         if (users.containsKey(user.getUsername())) {
//             throw new RuntimeException("Username already exists");
//         }
        
//         // Encrypt password before storing
//         user.setPassword(passwordEncoder.encode(user.getPassword()));
//         users.put(user.getUsername(), user);
//     }

//     public Optional<User> authenticate(String username, String password) {
//         User user = users.get(username);
//         if (user != null && passwordEncoder.matches(password, user.getPassword())) {
//             return Optional.of(user);
//         }
//         return Optional.empty();
//     }

//     public void logout(String username) {
//         sessionManager.invalidateSession(username);
//     }
// }
package com.auth.service;

import com.auth.cache.CacheService;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username {} already exists", user.getUsername());
            throw new RuntimeException("Username already exists");
        }

        // Set default role if not specified
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("USER");
        }

        // Encrypt password before storing
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Add the new user to the cache
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

    // Fetch all users (cached or from the database)
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
