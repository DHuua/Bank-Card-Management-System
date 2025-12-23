package com.example.bankcards.dto;

import com.example.bankcards.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {

    private Long id;
    private String maskedCardNumber; // **** **** **** 1234
    private String cardHolder;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private String ownerUsername;
}