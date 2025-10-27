package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserAuthService;
import com.example.bankcards.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminControllerV1.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AdminControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserAuthService userAuthService;

    @Test
    @WithMockUser(username = "admin@example.com")
    void blockCard_Success() throws Exception {
        CardEnteredDto cardDto = new CardEnteredDto();

        when(userService.isAdmin("admin@example.com")).thenReturn(true);
        doNothing().when(cardService).blockCard(any(CardEnteredDto.class));

        mockMvc.perform(post("/api/v1/admin/block-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void blockCard_NotAdmin_ReturnsForbidden() throws Exception {
        CardEnteredDto cardDto = new CardEnteredDto();

        when(userService.isAdmin("user@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/block-card")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void createCard_Success() throws Exception {
        CardEnteredDto cardDto = new CardEnteredDto();
        UUID userId = UUID.randomUUID();

        when(userService.isAdmin("admin@example.com")).thenReturn(true);
        doNothing().when(cardService).addCard(any(CardEnteredDto.class), any(UUID.class));

        mockMvc.perform(put("/api/v1/admin/create-card")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@example.com")
    void deleteUser_Success() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.isAdmin("admin@example.com")).thenReturn(true);
        doNothing().when(userService).deleteUser(any(UUID.class));

        mockMvc.perform(delete("/api/v1/admin/delete-user")
                        .param("userId", userId.toString()))
                .andExpect(status().isOk());
    }
}