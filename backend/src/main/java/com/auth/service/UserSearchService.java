// package com.auth.service;

// import com.auth.controller.UserSearchController;
// import com.auth.model.User;
// import com.auth.model.Tweet;
// import com.auth.repository.UserRepository;
// import com.auth.repository.TweetRepository;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import java.util.List;
// import java.util.Optional;

// @Service
// public class UserSearchService {

//     @Autowired
//     private UserRepository userRepository;

//     @Autowired
//     private TweetRepository tweetRepository;

//     public User getUserByUsername(String username) {
//         Optional<User> userOpt = userRepository.findByUsername(username);
//         return userOpt.orElse(null); // Return null if user not found
//     }
//     public List<Tweet> getTweetsByUser(String username) {
//         return tweetRepository.findByUserUsernameOrderByTimestampDesc(username);
//     }
//     // Method to get user details along with their tweets
//     public UserSearchController.UserSearchResponse getUserDetailsWithTweets(String username) {
//         // Initialize the response model
//         UserSearchController.UserSearchResponse response = new UserSearchController.UserSearchResponse(null, null);

//         // Fetch user details by username
//         Optional<User> userOpt = userRepository.findByUsername(username);
//         if (userOpt.isPresent()) {
//             User user = userOpt.get();
//             response.setUser(user);

//             // Fetch all tweets of the user sorted by timestamp in descending order
//             List<Tweet> tweets = tweetRepository.findByUserUsernameOrderByTimestampDesc(username);
//             response.setTweets(tweets);
//         } else {
//             // Handle case where user is not found
//             response.setUser(null);
//             response.setTweets(null);
//         }

//         return response;
//     }
// }
package com.auth.service;

import com.auth.cache.CacheService;
import com.auth.model.User;
import com.auth.model.Tweet;
import com.auth.repository.UserRepository;
import com.auth.repository.TweetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserSearchService {
    private static final Logger logger = LoggerFactory.getLogger(UserSearchService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private CacheService cacheService; // Inject CacheService for caching

    // Fetch user by username (first check cache, then database)
    public User getUserByUsername(String username) {
        // Check cache first
        User cachedUser = cacheService.getUserByUsername(username);
        if (cachedUser != null) {
            logger.info("User {} found in cache", username);
            return cachedUser;
        }

        // Fetch from database if not in cache
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            cacheService.addUserToCache(user); // Cache the user
            logger.info("User {} fetched from database and added to cache", username);
            return user;
        }

        logger.warn("User {} not found in database", username);
        return null; // User not found
    }

    // Fetch tweets by username (optionally, implement caching here)
    public List<Tweet> getTweetsByUser(String username) {
        logger.info("Fetching tweets for user {}", username);
        return tweetRepository.findByUserUsernameOrderByTimestampDesc(username);
    }
}

