
package com.auth.cache;

import com.auth.model.User;
import com.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    private TreeNode root; // Root of the binary search tree

    @Autowired
    private UserRepository userRepository; // Inject UserRepository for database interaction

    // Check if the cache is empty
    public boolean isCacheEmpty() {
        logger.info("Checking if cache is empty...");
        return root == null;
    }

    // Add a user to the cache
    public void addUserToCache(User user) {
        logger.info("Adding user to cache: {}", user.getUsername());
        root = insertRec(root, user);
    }

    // Recursive method to insert a user into the BST
    private TreeNode insertRec(TreeNode root, User user) {
        if (root == null) {
            logger.debug("Inserting user at a new tree node: {}", user.getUsername());
            return new TreeNode(user);
        }

        String username = user.getUsername();
        if (username.compareTo(root.getUser().getUsername()) < 0) {
            root.setLeft(insertRec(root.getLeft(), user));
        } else {
            root.setRight(insertRec(root.getRight(), user));
        }

        return root;
    }

    // Fetch a user by username
    public User getUserByUsername(String username) {
        logger.info("Fetching user for username: {}", username);
        TreeNode node = searchNodeForUsername(root, username);

        if (node == null) {
            logger.warn("No user found for username: {}", username);
            return null;
        }

        return node.getUser();
    }

    // Fetch all users
    public List<User> getAllUsersFromCache() {
        logger.info("Fetching all users from cache...");
        List<User> users = new ArrayList<>();
        traverseTree(root, users);
        return users;
    }

    // Search for a node by username
    private TreeNode searchNodeForUsername(TreeNode root, String username) {
        if (root == null) {
            return null;
        }

        String currentUsername = root.getUser().getUsername();
        if (currentUsername.equals(username)) {
            return root;
        }

        if (username.compareTo(currentUsername) < 0) {
            return searchNodeForUsername(root.getLeft(), username);
        }

        return searchNodeForUsername(root.getRight(), username);
    }

    // Traverse the tree and collect users
    private void traverseTree(TreeNode node, List<User> users) {
        if (node != null) {
            traverseTree(node.getLeft(), users);
            users.add(node.getUser());
            traverseTree(node.getRight(), users);
        }
    }

    // Scheduled method to refresh the cache every 10 minutes
    @Scheduled(fixedRate = 15000) // 600,000 ms = 10 minutes
    public void refreshCache() {
        logger.info("Refreshing cache with users from the database...");
        root = null; // Clear the current cache
        List<User> users = userRepository.findAll();
        for (User user : users) {
            addUserToCache(user);
        }
        logger.info("Cache refreshed with {} users.", users.size());
    }
}
