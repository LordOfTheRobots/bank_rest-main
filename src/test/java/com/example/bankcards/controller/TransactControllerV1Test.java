package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.CardEnteredDto;
import com.example.bankcards.service.TransactionsService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactControllerV1.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class TransactControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionsService transactionsService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com")
    void makeTransaction_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        CardEnteredDto cardDto = new CardEnteredDto();
        String whereToTransact = "1234567812345678";
        Double amount = 100.0;

        when(userService.isOwner(userId, "test@example.com")).thenReturn(true);
        doNothing().when(transactionsService).makeTransaction(any(CardEnteredDto.class), anyString(), any(UUID.class), anyDouble());

        mockMvc.perform(post("/api/v1/transaction/make-transaction")
                        .param("whereToTransact", whereToTransact)
                        .param("userId", userId.toString())
                        .param("howManyToTransact", amount.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardDto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void makeTransaction_NotOwner_ReturnsForbidden() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.isOwner(userId, "test@example.com")).thenReturn(false);

        mockMvc.perform(post("/api/v1/transaction/make-transaction")
                        .param("whereToTransact", "1234567812345678")
                        .param("userId", userId.toString())
                        .param("howManyToTransact", "100.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardEnteredDto())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void makeTransaction_InvalidAmount_ReturnsBadRequest() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.isOwner(userId, "test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/v1/transaction/make-transaction")
                        .param("whereToTransact", "1234567812345678")
                        .param("userId", userId.toString())
                        .param("howManyToTransact", "-100.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CardEnteredDto())))
                .andExpect(status().isBadRequest());
    }
}
