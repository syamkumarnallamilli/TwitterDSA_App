package com.auth.controller;

import com.auth.model.Tweet;
import com.auth.service.TweetService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tweets")
@CrossOrigin(origins = "http://localhost:3000")
public class TweetController {
    
    @Autowired
    private TweetService tweetService;

    @Data
    public static class TweetRequest {
        private String content;
    }

    @GetMapping
    public ResponseEntity<List<Tweet>> getTweets() {
        List<Tweet> tweets = tweetService.getTweets();
        return ResponseEntity.ok(tweets);
    }

    @PostMapping
    public ResponseEntity<?> createTweet(
        @RequestBody TweetRequest request,
        @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Tweet tweet = tweetService.createTweet(
                request.getContent(),
                userDetails.getUsername()
            );
            return ResponseEntity.ok(tweet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to create tweet"));
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<?> getUserTweets(
        
        @PathVariable String username
    ) {
        try {
            List<Tweet> tweets = tweetService.getUserTweets(username);
            return ResponseEntity.ok(tweets);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
