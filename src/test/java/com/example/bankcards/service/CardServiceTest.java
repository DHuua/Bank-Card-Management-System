package com.example.bankcards.service;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMaskingUtil cardMaskingUtil;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .role(Role.USER)
                .isActive(true)
                .build();

        card = Card.builder()
                .id(1L)
                .cardNumber("encrypted-card-number")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2027, 12, 31))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000.00))
                .owner(user)
                .build();

        createRequest = CardCreateRequest.builder()
                .cardNumber("4532015112830366")
                .cardHolder("TEST USER")
                .expiryDate(LocalDate.of(2027, 12, 31))
                .cvv("123")
                .initialBalance(BigDecimal.valueOf(1000.00))
                .userId(1L)
                .build();
    }

    @Test
    void testCreateCard_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cardMaskingUtil.isValidCardNumber("4532015112830366")).thenReturn(true);
        when(encryptionUtil.encrypt("4532015112830366")).thenReturn("encrypted-card-number");
        when(encryptionUtil.encrypt("123")).thenReturn("encrypted-cvv");
        when(cardRepository.existsByCardNumber("encrypted-card-number")).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(encryptionUtil.decrypt("encrypted-card-number")).thenReturn("4532015112830366");
        when(cardMaskingUtil.maskCardNumber("4532015112830366")).thenReturn("**** **** **** 0366");

        CardDTO result = cardService.createCard(createRequest);

        assertNotNull(result);
        assertEquals("**** **** **** 0366", result.getMaskedCardNumber());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void testCreateCard_InvalidCardNumber() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cardMaskingUtil.isValidCardNumber("4532015112830366")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> cardService.createCard(createRequest));

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testCreateCard_CardAlreadyExists() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cardMaskingUtil.isValidCardNumber("4532015112830366")).thenReturn(true);
        when(encryptionUtil.encrypt("4532015112830366")).thenReturn("encrypted-card-number");
        when(cardRepository.existsByCardNumber("encrypted-card-number")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> cardService.createCard(createRequest));

        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void testBlockCard_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);
        when(encryptionUtil.decrypt(anyString())).thenReturn("4532015112830366");
        when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 0366");

        CardDTO result = cardService.blockCard(1L);

        assertNotNull(result);
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testGetCardById_NotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cardService.getCardEntity(999L));
    }
}