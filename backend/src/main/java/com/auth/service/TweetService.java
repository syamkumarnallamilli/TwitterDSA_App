package com.auth.service;

import com.auth.model.Tweet;
import com.auth.model.User;
import com.auth.repository.TweetRepository;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TweetService {
    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    public Tweet createTweet(String content, String username) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Tweet content cannot be empty");
        }
        if (content.length() > 280) {
            throw new IllegalArgumentException("Tweet content cannot exceed 280 characters");
        }

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Tweet tweet = new Tweet();
        tweet.setContent(content.trim());
        tweet.setTimestamp(LocalDateTime.now());
        tweet.setUser(user);

        return tweetRepository.save(tweet);
    }

    public List<Tweet> getUserTweets(String username) {
        
        return tweetRepository.findByUserUsernameOrderByTimestampDesc(
            username
        );
    }

    public List<Tweet> getTweets() {
        return tweetRepository.findAllByOrderByTimestampDesc();
    }
}