package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public boolean isOwnerOrAdmin(UUID userId, String currentUserEmail) {
        logger.debug("Checking if user {} is owner or admin for user {}", currentUserEmail, userId);
        return isOwner(userId, currentUserEmail) || isAdmin(currentUserEmail);
    }

    public User findUserById(UUID userId){
        logger.debug("Finding user by ID: {}", userId);
        return userRepository.findByUserId(userId).orElseThrow(
                () -> new NotFound("User not found"));
    }

    public boolean isOwner(UUID userId, String email) {
        logger.debug("Checking if user {} is owner of account {}", email, userId);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getUserId().equals(userId);
    }

    public boolean isAdmin(String email) {
        logger.debug("Checking if user {} is admin", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getRole().getRoleName().contains("ADMIN");
    }

    public void deleteUser(UUID userID){
        logger.info("Deleting user: {}", userID);
        userRepository.deleteByUserId(userID);
        logger.info("User deleted successfully: {}", userID);
    }

    public Boolean userExist(UUID userId){
        logger.debug("Checking if user exists: {}", userId);
        return userRepository.existsByUserId(userId);
    }
}
