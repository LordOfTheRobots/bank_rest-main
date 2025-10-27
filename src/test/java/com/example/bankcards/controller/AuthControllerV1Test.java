package com.example.bankcards.controller;

import com.example.bankcards.TestRepositoryConfig;
import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.UserAuthDto;
import com.example.bankcards.exception.PasswordIsNotValid;
import com.example.bankcards.service.UserAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthControllerV1.class)
@Import({TestSecurityConfig.class, TestRepositoryConfig.class})
@ActiveProfiles("test")
class AuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserAuthService authService;



    @Test
    void signUp_Success() throws Exception {
        UserAuthDto userDto = new UserAuthDto();
        userDto.setEmail("test@example.com");
        userDto.setPassword("Password123!");

        AuthResponse authResponse = new AuthResponse(
                "jwtToken",
                "refreshToken",
                UUID.randomUUID()
        );

        when(authService.createUser(any(UserAuthDto.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.jwt").value("jwtToken"))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"));
    }

    @Test
    void signUp_InvalidData_ReturnsBadRequest() throws Exception {
        UserAuthDto userDto = new UserAuthDto();
        when(authService.createUser(userDto)).thenThrow(PasswordIsNotValid.class);
        mockMvc.perform(post("/api/v1/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest());
    }
}
