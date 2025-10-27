package com.example.bankcards.service;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.NotFound;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void isOwnerOrAdmin_ReturnsTrueForOwner() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = userService.isOwnerOrAdmin(userId, email);

        assertTrue(result);
    }

    @Test
    void isOwnerOrAdmin_ReturnsTrueForAdmin() {
        UUID userId = UUID.randomUUID();
        String email = "admin@example.com";
        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setRole(new Role());
        user.getRole().setRoleName("ADMIN");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = userService.isOwnerOrAdmin(userId, email);

        assertTrue(result);
    }

    @Test
    void isOwner_ReturnsTrue() {
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        User user = new User();
        user.setUserId(userId);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = userService.isOwner(userId, email);

        assertTrue(result);
    }

    @Test
    void isAdmin_ReturnsTrue() {
        String email = "admin@example.com";
        User user = new User();
        user.setRole(new Role());
        user.getRole().setRoleName("ADMIN");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        boolean result = userService.isAdmin(email);

        assertTrue(result);
    }

    @Test
    void findUserById_Success() {
        UUID userId = UUID.randomUUID();
        User user = new User();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        User result = userService.findUserById(userId);

        assertNotNull(result);
        verify(userRepository).findByUserId(userId);
    }

    @Test
    void findUserById_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(NotFound.class, () -> userService.findUserById(userId));
    }

    @Test
    void deleteUser_Success() {
        UUID userId = UUID.randomUUID();

        userService.deleteUser(userId);

        verify(userRepository).deleteByUserId(userId);
    }

    @Test
    void userExist_ReturnsTrue() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByUserId(userId)).thenReturn(true);

        boolean result = userService.userExist(userId);

        assertTrue(result);
    }
}
