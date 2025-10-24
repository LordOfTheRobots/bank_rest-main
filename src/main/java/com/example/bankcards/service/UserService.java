package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean isOwnerOrAdmin(UUID userId, String currentUserEmail) {
        return isOwner(userId, currentUserEmail) || isAdmin(currentUserEmail);
    }

    public User findUserById(UUID userId){
        return userRepository.findByUserId(userId).orElseThrow(
                () -> new NotFound("User not found"));
    }

    public boolean isOwner(UUID userId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getUserId().equals(userId);
    }

    public boolean isAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFound("User not found"));
        return user.getRole().getRoleName().contains("ADMIN");
    }

    public void deleteUser(UUID userID){
        userRepository.deleteByUserId(userID);
    }

    public Boolean userExist(UUID userId){
        return userRepository.existsByUserId(userId);
    }
}
