package com.example.bankcards.controller;

import com.example.bankcards.TestSecurityConfig;
import com.example.bankcards.dto.CardToShowDto;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserCardsControllerV1.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class UserCardsControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@example.com")
    void showCards_Success() throws Exception {
        UUID userId = UUID.randomUUID();
        CardToShowDto cardDto = createValidCardDto();

        Page<CardToShowDto> page = new PageImpl<>(
                List.of(cardDto),
                PageRequest.of(0, 10),
                1
        );

        when(userService.isOwner(eq(userId), anyString())).thenReturn(true);
        when(cardService.showCards(eq(userId), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/{userId}/show-cards", userId.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showCards_EmptyResult() throws Exception {
        UUID userId = UUID.randomUUID();

        Page<CardToShowDto> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 10),
                0
        );

        when(userService.isOwner(eq(userId), anyString())).thenReturn(true);
        when(cardService.showCards(eq(userId), eq(0), eq(10))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/users/{userId}/show-cards", userId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showCards_AccessDenied_ReturnsForbidden() throws Exception {
        UUID userId = UUID.randomUUID();

        when(userService.isOwner(eq(userId), anyString())).thenReturn(false);

        mockMvc.perform(get("/api/v1/users/{userId}/show-cards", userId.toString())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showCards_WithDefaultPagination() throws Exception {
        UUID userId = UUID.randomUUID();
        CardToShowDto cardDto = createValidCardDto();

        Page<CardToShowDto> page = new PageImpl<>(
                List.of(cardDto),
                PageRequest.of(0, 10),
                1
        );

        when(userService.isOwner(eq(userId), anyString())).thenReturn(true);
        when(cardService.showCards(eq(userId), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/{userId}/show-cards", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].cardMask  ").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void showCards_WithCustomPagination() throws Exception {
        UUID userId = UUID.randomUUID();
        CardToShowDto cardDto1 = createValidCardDto();
        CardToShowDto cardDto2 = createValidCardDto();
        cardDto2.setCardMask("9876543210987654");

        Page<CardToShowDto> page = new PageImpl<>(
                List.of(cardDto1, cardDto2),
                PageRequest.of(1, 5),
                15
        );

        when(userService.isOwner(eq(userId), anyString())).thenReturn(true);
        when(cardService.showCards(eq(userId), eq(1), eq(5))).thenReturn(page);

        mockMvc.perform(get("/api/v1/users/{userId}/show-cards", userId.toString())
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    private CardToShowDto createValidCardDto() {
        CardToShowDto dto = new CardToShowDto();
        dto.setCardMask("************3456");
        dto.setExpireDate("12/25");
        return dto;
    }
}