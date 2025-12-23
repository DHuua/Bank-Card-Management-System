package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private static final SecureRandom random = new SecureRandom();

    /**
     * Генерирует валидный номер карты по алгоритму Luhn
     * Начинается с 4 (Visa) или 5 (MasterCard)
     */
    public String generateCardNumber() {
        // Генерируем префикс (4 для Visa, 5 для MasterCard)
        String prefix = random.nextBoolean() ? "4" : "5";
        
        // Генерируем остальные 15 цифр
        StringBuilder cardNumber = new StringBuilder(prefix);
        for (int i = 0; i < 14; i++) {
            cardNumber.append(random.nextInt(10));
        }
        
        // Добавляем контрольную цифру по Luhn
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);
        
        return cardNumber.toString();
    }

    /**
     * Вычисляет контрольную цифру по алгоритму Luhn
     */
    private int calculateLuhnCheckDigit(String partialCardNumber) {
        int sum = 0;
        boolean alternate = true;
        
        for (int i = partialCardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(partialCardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (10 - (sum % 10)) % 10;
    }

    /**
     * Генерирует CVV код (3 цифры)
     */
    public String generateCVV() {
        return String.format("%03d", random.nextInt(1000));
    }
}