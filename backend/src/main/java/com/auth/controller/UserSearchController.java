// package com.auth.controller;

// import com.auth.cache.CacheService;
// import com.auth.model.User;
// import com.auth.model.Tweet;
// import com.auth.service.UserSearchService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/search")
// public class UserSearchController {

//     @Autowired
//     private CacheService cacheService; // Injecting CacheService for caching

//     @Autowired
//     private UserSearchService userSearchService; // Injecting UserSearchService

//     @GetMapping("/user/{username}")
//     public UserSearchResponse searchUser(@PathVariable String username) {
//         // Try to fetch the user from the cache
//         User user = cacheService.getUserByUsername(username);

//         // If the user is not found in the cache, fetch it from the database via UserSearchService
//         if (user == null) {
//             user = userSearchService.getUserByUsername(username); // Fetch user from DB

//             // If the user is found in the database, cache it
//             if (user != null) {
//                 cacheService.addUserToCache(user);
//             }
//         }

//         // Fetch the user's tweets (tweets will always be fetched from DB, assuming no caching on tweets)
//         List<Tweet> tweets = userSearchService.getTweetsByUser(username);

//         // Return the user and their tweets
//         return new UserSearchResponse(user, tweets);
//     }

//     // Response model to include user and their tweets
//     public static class UserSearchResponse {
//         private User user;
//         private List<Tweet> tweets;

//         // Constructor
//         public UserSearchResponse(User user, List<Tweet> tweets) {
//             this.user = user;
//             this.tweets = tweets;
//         }

//         // Getters and setters
//         public User getUser() {
//             return user;
//         }

//         public void setUser(User user) {
//             this.user = user;
//         }

//         public List<Tweet> getTweets() {
//             return tweets;
//         }

//         public void setTweets(List<Tweet> tweets) {
//             this.tweets = tweets;
//         }
//     }
// }
package com.auth.controller;

import com.auth.cache.CacheService;
import com.auth.model.User;
import com.auth.model.Tweet;
import com.auth.service.UserSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class UserSearchController {

    private static final Logger logger = LoggerFactory.getLogger(UserSearchController.class);

    @Autowired
    private UserSearchService userSearchService;

    @GetMapping("/user/{username}")
    public UserSearchResponse searchUser(@PathVariable String username) {
        // Validate username
        if (username == null || username.trim().isEmpty()) {
            logger.error("Invalid username provided");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be null or empty");
        }

        // Fetch user details
        User user = userSearchService.getUserByUsername(username);
        if (user == null) {
            logger.warn("User {} not found", username);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Fetch tweets for the user
        List<Tweet> tweets = userSearchService.getTweetsByUser(username);

        logger.info("User {} and their tweets fetched successfully", username);

        return new UserSearchResponse(user, tweets);
    }

    // Response model to include user and their tweets
    public static class UserSearchResponse {
        private User user;
        private List<Tweet> tweets;

        // Constructor
        public UserSearchResponse(User user, List<Tweet> tweets) {
            this.user = user;
            this.tweets = tweets;
        }

        // Getters and setters
        public User getUser() {
            return user;
        } 

        public void setUser(User user) {
            this.user = user;
        }

        public List<Tweet> getTweets() {
            return tweets;
        }

        public void setTweets(List<Tweet> tweets) {
            this.tweets = tweets;
        }
    }
}
