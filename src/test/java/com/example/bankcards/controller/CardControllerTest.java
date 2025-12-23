package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @Test
    void testGetCardById_Success() throws Exception {
        CardDTO cardDTO = CardDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .expiryDate(LocalDate.of(2027, 12, 31))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .ownerUsername("john_doe")
                .build();

        when(cardService.getCardById(1L)).thenReturn(cardDTO);

        mockMvc.perform(get("/api/cards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maskedCardNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void testGetMyCards_Success() throws Exception {
        CardDTO card1 = CardDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .cardHolder("JOHN DOE")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .build();

        CardDTO card2 = CardDTO.builder()
                .id(2L)
                .maskedCardNumber("**** **** **** 5678")
                .cardHolder("JOHN DOE")
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500.00))
                .build();

        Page<CardDTO> page = new PageImpl<>(List.of(card1, card2), PageRequest.of(0, 10), 2);

        when(cardService.getUserCards(any())).thenReturn(page);

        mockMvc.perform(get("/api/cards/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].maskedCardNumber").value("**** **** **** 1234"));
    }

    @Test
    void testBlockCard_Success() throws Exception {
        CardDTO cardDTO = CardDTO.builder()
                .id(1L)
                .maskedCardNumber("**** **** **** 1234")
                .status(CardStatus.BLOCKED)
                .build();

        when(cardService.blockCard(1L)).thenReturn(cardDTO);

        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }
}