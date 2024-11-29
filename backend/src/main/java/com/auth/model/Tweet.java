package com.auth.model;

import lombok.Data;//Lombok is a Java library that automatically generates boilerplate code (like getters, setters, constructors) to reduce verbosity
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*; //Java Persistence API (JPA) annotations used to map this class to a relational database table.
import java.time.LocalDateTime;//which will store the timestamp of when a tweet was created.

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tweets")
public class Tweet {
    @Id //pk
    @GeneratedValue(strategy = GenerationType.IDENTITY) //autoincrement
    private Long id;
    // If a new Tweet is saved to the database, the id is automatically assigned (e.g., 1, 2, 3, etc.).

    @Column(nullable = false, length = 280) //Limits the text content to 280 characters (like Twitter’s character limit).
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;//Stores the exact date and time the tweet was created.

    @ManyToOne(fetch = FetchType.LAZY)
    //@ManyToOne: Indicates a many-to-one relationship. Each Tweet is associated with a single User, but each User can have multiple Tweets.
    @JoinColumn(name = "user_id", nullable = false)
    //Sets up a foreign key column named "user_id" in the "tweets" table, linking each tweet to a specific user. nullable = false makes this relationship mandatory.
    private User user;
}

//FetchType.LAZY is often beneficial when you don’t always need the related data immediately. 
//For example, if you load a Tweet, but you only need the content and timestamp without the associated User data, lazy loading can optimize performance by avoiding an additional database call.

// package com.auth.model;

// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.AllArgsConstructor;
// import org.springframework.data.annotation.Id;
// //import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.DBRef;

// import java.time.LocalDateTime;

// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// @Document(collection = "tweets") // Maps this class to the "tweets" collection in MongoDB
// public class Tweet {
//     @Id
//     private String id; // MongoDB's primary key is a string (_id).

//     private String content; // Content of the tweet (up to 280 characters).

//     private LocalDateTime timestamp; // Timestamp when the tweet was created.

//     @DBRef // References a User object in the "users" collection.
//     private User user;
// }
