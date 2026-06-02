package com.example.bankcards.controller.api;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthControllerV1 {
    private static final Logger USER_LOG = LoggerFactory.getLogger("USER_LOG");
    private final UserAuthService authService;

    @Value("${jwt.prefix}")
    private String jwtPrefix;

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody UserAuthDto dto, HttpServletResponse response) {
        try {
            USER_LOG.info("Sign in attempt for user: {}", dto.getEmail());
            AuthResponse authResponse = authService.authenticateUser(dto);
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            USER_LOG.info("Sign in successful for user: {}", dto.getEmail());
            return ResponseEntity.ok().header("Authorization", jwtPrefix + authResponse.getJwt()).body(authResponse);
        } catch (Exception e) {
            USER_LOG.error("Sign in failed for user {}: {}", dto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserAuthDto dto, HttpServletResponse response) {
        try {
            USER_LOG.info("Sign up attempt for user: {}", dto.getEmail());
            AuthResponse authResponse = authService.createUser(dto);
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            USER_LOG.info("Sign up successful for user: {}", dto.getEmail());
            return ResponseEntity.ok().header("Authorization", jwtPrefix + authResponse.getJwt()).body(authResponse);
        } catch (Exception e) {
            USER_LOG.error("Sign up failed for user {}: {}", dto.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        try {
            USER_LOG.debug("Refresh token request received");
            AuthResponse authResponse = authService.refreshTokens(refreshToken);
            setRefreshTokenCookie(response, authResponse.getRefreshToken());
            USER_LOG.info("Token refreshed successfully");
            return ResponseEntity.ok().header("Authorization", jwtPrefix + authResponse.getJwt()).body(authResponse);
        } catch (Exception e) {
            USER_LOG.error("Token refresh failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true).secure(true).path("/api/v1/auth/refresh")
                .maxAge(24 * 60 * 60).sameSite("Strict").build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}