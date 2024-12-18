
package com.auth.cache;

import com.auth.model.User;

// Represents a node in the binary search tree for caching
public class TreeNode {
    private User user; // User data
    private TreeNode left; // Left child in the BST
    private TreeNode right; // Right child in the BST

    // Constructor to initialize a TreeNode with a User
    public TreeNode(User user) {
        this.user = user;
        this.left = null;
        this.right = null;
    }

    // Getters and setters for user, left, and right
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public TreeNode getLeft() {
        return left;
    }

    public void setLeft(TreeNode left) {
        this.left = left;
    }

    public TreeNode getRight() {
        return right;
    }

    public void setRight(TreeNode right) {
        this.right = right;
    }
}
