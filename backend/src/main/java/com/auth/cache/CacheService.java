
package com.auth.cache;

import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import javax.swing.Spring;

@Service  //The @Service annotation makes this class a Spring service, meaning it is managed by the Spring container and can be injected where needed.
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);//initializes a logger
    private static final int MAX_CACHE_SIZE = 10000; // Maximum number of users cache can hold before eviction starts
    private TreeNode root; // stores root node of bst
    private int size = 0;//current number of users in cache
    private Map<String, Long> lastAccessTime = new HashMap<>();
    private Map<String, Integer> cacheHits = new HashMap<>();
    private long totalRequests = 0;
    private long cacheHitCount = 0;
    private long cacheMissCount = 0;

    @Autowired
    private UserRepository userRepository;//injects user repository to fetch user data when user is not present in cache
    public boolean isCacheEmpty() {
        

                logger.info("Checking if cache is empty...");
                return root == null;
            }
        
    // Add a user to the cache
    public void addUserToCache(User user) {
        logger.info("Adding user to cache: {}", user.getUsername());
        if (size >= MAX_CACHE_SIZE)
        
        {
            evictLeastRecentlyUsed();
        }
        
        root = insertRec(root, user);
        lastAccessTime.put(user.getUsername(), System.currentTimeMillis());
        size++;
    }


    private TreeNode insertRec(TreeNode root, User user) {
        if (root == null) {
            return new TreeNode(user);
        }

        if (user.getUsername().compareTo(root.getUser().getUsername()) < 0) {
            root.setLeft(insertRec(root.getLeft(), user));
        } else {
            root.setRight(insertRec(root.getRight(), user));
        }

        return root;//return updated root node after insertion
    }

    // Retrieve user by username
    public User getUserByUsername(String username) {
        logger.info("Fetching user for username: {}", username);
        totalRequests++;

        TreeNode node = searchNodeForUsername(root, username);
        
        if (node == null) {
            logger.warn("Cache MISS for username: {}", username);
            cacheMissCount++;
            User userFromDB = userRepository.findByUsername(username).orElse(null);
            if (userFromDB != null) {
                addUserToCache(userFromDB);//Fetches the user from the database and adds it to the cache if found.
            }
            return userFromDB;
        }
        logger.info("Cache HIT for username: {}", username);
        cacheHitCount++;
        cacheHits.merge(username, 1, Integer::sum);
        lastAccessTime.put(username, System.currentTimeMillis());
        return node.getUser();
    }

    // Search for a TreeNode by username
    private TreeNode searchNodeForUsername(TreeNode root, String username) {
        if (root == null) {
            return null;
        }

        int comparison = username.compareTo(root.getUser().getUsername());
        if (comparison == 0) {
            return root;
        }
        return comparison < 0 ? searchNodeForUsername(root.getLeft(), username)
                              : searchNodeForUsername(root.getRight(), username);
    }

    public Map<String, Object> getCacheStats() {
              Map<String, Object> stats = new HashMap<>();
                stats.put("size", size);
                stats.put("maxSize", MAX_CACHE_SIZE);
        stats.put("totalRequests", totalRequests);
               stats.put("cacheHits", cacheHitCount);
                stats.put("cacheMisses", cacheMissCount);
             stats.put("hitRate", totalRequests > 0 ? (double) cacheHitCount / totalRequests : 0);
            stats.put("mostAccessedUsers", getMostAccessedUsers(5));
                return stats;
        }
        private List<Map<String, Object>> getMostAccessedUsers(int limit) {
                    return cacheHits.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(limit)
                        .map(entry -> {
                            Map<String, Object> userStats = new HashMap<>();
                            userStats.put("username", entry.getKey());
                            userStats.put("hits", entry.getValue());
                            userStats.put("lastAccessed", lastAccessTime.get(entry.getKey()));
                            return userStats;
                        })
                        .collect(Collectors.toList());
                }
            
    

    private void evictLeastRecentlyUsed() {
        String lruUser = lastAccessTime.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (lruUser != null) {
            logger.info("Evicting least recently used user: {}", lruUser);
            removeFromCache(lruUser);
            lastAccessTime.remove(lruUser);
            size--;
        }
    }

    // Remove a user from the cache
    private void removeFromCache(String username) {
        root = removeRec(root, username);
    }

    // Recursive deletion of a user from the BST
    private TreeNode removeRec(TreeNode root, String username) {
        if (root == null) {
            return null;
        }

        int comparison = username.compareTo(root.getUser().getUsername());
        if (comparison < 0) {
            root.setLeft(removeRec(root.getLeft(), username));
        } else if (comparison > 0) {
            root.setRight(removeRec(root.getRight(), username));
        } else {
            if (root.getLeft() == null) return root.getRight();
            if (root.getRight() == null) return root.getLeft();

            TreeNode successor = findMin(root.getRight());
            root.setUser(successor.getUser());
            root.setRight(removeRec(root.getRight(), successor.getUser().getUsername()));
        }
        return root;
    }

    // Find the node with the smallest value in the BST
    private TreeNode findMin(TreeNode root) {
        while (root.getLeft() != null) {
            root = root.getLeft();
        }
        return root;
    }

    // Scheduled task to refresh cache every 5 minutes
    @Scheduled(fixedRate = 15000)
    public void refreshCache() {
        // logger.info("Refreshing cache...");
        logger.info("Cache refresh started at {}", new Date());
    // Your refresh logic
    logger.info("Cache refresh completed at {}", new Date());
        List<User> dbUsers = userRepository.findAll();
        Set<String> dbUsernames = dbUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.toSet());

        getAllUsersFromCache().stream()
                .map(User::getUsername)
                .filter(username -> !dbUsernames.contains(username))
                .forEach(username -> {
                    removeFromCache(username);
                    lastAccessTime.remove(username);
                    size--;
                    logger.info("Removed user from cache: {}", username);
                });

        dbUsers.forEach(this::addUserToCache);
        logger.info("Cache refresh completed.");
    }

    // Fetch all users from cache
    public List<User> getAllUsersFromCache() {
        List<User> users = new ArrayList<>();
        traverseTree(root, users);
        return users;
    }

    private void traverseTree(TreeNode node, List<User> users) {
        if (node != null) {
            traverseTree(node.getLeft(), users);
            users.add(node.getUser());
            traverseTree(node.getRight(), users);
        }
    }
}
 