package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
public class CardCreateRequest {

    @NotBlank(message = "Card number is required")
    @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    private String cardNumber;

    @NotBlank(message = "Card holder name is required")
    private String cardHolder;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @Pattern(regexp = "\\d{3}", message = "CVV must be 3 digits")
    private String cvv;

    private BigDecimal initialBalance;

    @NotNull(message = "User ID is required")
    private Long userId; // для админа, чтобы создать карту для пользователя
}