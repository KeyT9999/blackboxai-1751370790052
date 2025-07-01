package com.emailreader.dao;

import com.emailreader.model.User;
import com.emailreader.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT id, username, password_hash, role FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(rs.getString("password_hash"));
                    user.setRole(rs.getString("role"));
                    return Optional.of(user);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding user by username: {}", username, e);
            throw new RuntimeException("Database error occurred", e);
        }
        
        return Optional.empty();
    }

    public User create(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?) RETURNING id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // Hash the password
            String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
            
            stmt.setString(1, username);
            stmt.setString(2, passwordHash);
            stmt.setString(3, role);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setUsername(username);
                    user.setPasswordHash(passwordHash);
                    user.setRole(role);
                    return user;
                } else {
                    throw new RuntimeException("Failed to create user");
                }
            }
        } catch (SQLException e) {
            logger.error("Error creating user: {}", username, e);
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = findByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return BCrypt.checkpw(password, user.getPasswordHash());
        }
        
        return false;
    }

    public List<User> findAllUsers() {
        String sql = "SELECT id, username, password_hash, role FROM users";
        List<User> users = new ArrayList<>();
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Database error occurred", e);
        }
        
        return users;
    }

    public void updatePassword(Long userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String passwordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            
            stmt.setString(1, passwordHash);
            stmt.setLong(2, userId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new RuntimeException("Failed to update password");
            }
        } catch (SQLException e) {
            logger.error("Error updating password for user ID: {}", userId, e);
            throw new RuntimeException("Database error occurred", e);
        }
    }

    public void delete(Long userId) {
        String sql = "DELETE FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, userId);
            
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected != 1) {
                throw new RuntimeException("Failed to delete user");
            }
        } catch (SQLException e) {
            logger.error("Error deleting user ID: {}", userId, e);
            throw new RuntimeException("Database error occurred", e);
        }
    }
}
