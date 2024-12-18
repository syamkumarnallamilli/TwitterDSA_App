package com.auth.repository;

import com.auth.model.Tweet;

import java.util.List;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface TweetRepository extends JpaRepository<Tweet, Long> {
    List<Tweet> findByUserUsernameOrderByTimestampDesc(String username);
    List<Tweet> findAllByOrderByTimestampDesc();
}
