package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class AuthControllerV1 {
    private static final Logger logger = LoggerFactory.getLogger(AuthControllerV1.class);

    @Autowired
    private final UserAuthService authService;

    @Value("${jwt.prefix}")
    private String jwtPrefix;

    @PostMapping
    @RequestMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody UserAuthDto userAuthDto,
                                               HttpServletResponse response){
        logger.info("Sign in attempt for user: {}", userAuthDto.getEmail());
        AuthResponse authResponse = authService.authenticateUser(userAuthDto);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        logger.info("Sign in successful for user: {}", userAuthDto.getEmail());
        return ResponseEntity.ok()
                .header("Authorization", jwtPrefix + authResponse.getJwt())
                .body(authResponse);
    }

    @PostMapping
    @RequestMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserAuthDto userAuthDto,
                                               HttpServletResponse response){
        logger.info("Sign up attempt for user: {}", userAuthDto.getEmail());
        AuthResponse authResponse = authService.createUser(userAuthDto);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        logger.info("Sign up successful for user: {}", userAuthDto.getEmail());
        return ResponseEntity.ok()
                .header("Authorization", jwtPrefix + authResponse.getJwt())
                .body(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        logger.debug("Refresh token request");
        AuthResponse authResponse = authService.refreshTokens(refreshToken);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        logger.info("Token refreshed successfully");

        return ResponseEntity.ok()
                .header("Authorization", jwtPrefix + authResponse.getJwt())
                .body(authResponse);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/refresh")
                .maxAge(30 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}