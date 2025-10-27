package com.example.bankcards.service;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.PasswordIsNotValid;
import com.example.bankcards.exception.UserAlreadyExist;
import com.example.bankcards.exception.UserDoesNotExistOrPasswordIncorrect;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.security.UserDetailService;
import com.example.bankcards.util.PasswordValidator;
import com.example.bankcards.util.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DtoMapper<User, UserAuthDto> mapper;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtTokenProvider jwtProvider;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private UserDetailService userDetailService;

    @InjectMocks
    private UserAuthService userAuthService;

    @Test
    void createUser_Success() {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        User user = new User();
        user.setUserId(UUID.randomUUID());
        user.setEmail("test@example.com");

        Authentication auth = mock(Authentication.class);

        when(passwordValidator.isValid("password")).thenReturn(true);
        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(mapper.map(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userDetailService.createAuthentication("test@example.com")).thenReturn(auth);
        when(jwtProvider.generateAccessToken(auth)).thenReturn("accessToken");
        when(jwtProvider.generateRefreshToken(auth)).thenReturn("refreshToken");

        AuthResponse result = userAuthService.createUser(userDto);

        assertNotNull(result);
        assertEquals("accessToken", result.getJwt());
        verify(userRepository).save(user);
        verify(passwordValidator).isValid("password");
    }

    @Test
    void createUser_PasswordInvalid_ThrowsException() {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setPassword("weak");

        when(passwordValidator.isValid("weak")).thenReturn(false);

        assertThrows(PasswordIsNotValid.class, () -> userAuthService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_UserAlreadyExists_ThrowsException() {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setEmail("existing@example.com");
        userDto.setPassword("password");

        when(passwordValidator.isValid("password")).thenReturn(true);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExist.class, () -> userAuthService.createUser(userDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticateUser_Success() {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("password");

        User user = new User();
        user.setUserId(UUID.randomUUID());


        when(encoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.findByEmailAndPassword("test@example.com", "encodedPassword"))
                .thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(any())).thenReturn("accessToken");

        AuthResponse result = userAuthService.authenticateUser(userDto);

        assertNotNull(result);
        assertEquals("accessToken", result.getJwt());
        verify(userRepository).findByEmailAndPassword("test@example.com", "encodedPassword");
    }

    @Test
    void authenticateUser_InvalidCredentials_ThrowsException() {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("wrong");

        when(encoder.encode("wrong")).thenReturn("encodedWrong");
        when(userRepository.findByEmailAndPassword("test@example.com", "encodedWrong"))
                .thenReturn(Optional.empty());

        assertThrows(UserDoesNotExistOrPasswordIncorrect.class,
                () -> userAuthService.authenticateUser(userDto));
    }
}
