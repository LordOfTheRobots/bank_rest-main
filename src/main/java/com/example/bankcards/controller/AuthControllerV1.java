package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.service.UserAuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/")
@AllArgsConstructor
public class AuthControllerV1 {

    @Autowired
    private UserAuthService authService;

    @Value("${jwt.prefix}")
    private String jwtPrefix;

    @PostMapping
    @RequestMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody UserAuthDto userAuthDto,
                                               HttpServletResponse response){
        AuthResponse authResponse = authService.authenticateUser(userAuthDto);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header("Authorization", jwtPrefix + authResponse.getJwt())
                .body(authResponse);
    }

    @PostMapping
    @RequestMapping("/sign-up")
    public ResponseEntity<AuthResponse> signUp(@Valid @RequestBody UserAuthDto userAuthDto,
                                               HttpServletResponse response){
        AuthResponse authResponse = authService.createUser(userAuthDto);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());
        return ResponseEntity.ok()
                .header("Authorization", jwtPrefix + authResponse.getJwt())
                .body(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.refreshTokens(refreshToken);
        setRefreshTokenCookie(response, authResponse.getRefreshToken());

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
