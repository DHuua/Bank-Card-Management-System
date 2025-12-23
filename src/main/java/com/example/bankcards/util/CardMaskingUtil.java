package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardMaskingUtil {

    /**
     * Маскирует номер карты, показывая только последние 4 цифры
     * Пример: 1234567890123456 -> **** **** **** 3456
     */
    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        // Убираем все пробелы если есть
        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        
        if (cleanNumber.length() != 16) {
            return "**** **** **** " + cleanNumber.substring(Math.max(0, cleanNumber.length() - 4));
        }

        // Берем последние 4 цифры
        String lastFour = cleanNumber.substring(12);
        
        return "**** **** **** " + lastFour;
    }

    /**
     * Форматирует номер карты с пробелами
     * Пример: 1234567890123456 -> 1234 5678 9012 3456
     */
    public String formatCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return "";
        }

        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        
        if (cleanNumber.length() != 16) {
            return cleanNumber;
        }

        return cleanNumber.substring(0, 4) + " " +
               cleanNumber.substring(4, 8) + " " +
               cleanNumber.substring(8, 12) + " " +
               cleanNumber.substring(12);
    }

    /**
     * Проверяет валидность номера карты по алгоритму Luhn
     */
    public boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return false;
        }

        String cleanNumber = cardNumber.replaceAll("\\s+", "");
        
        if (cleanNumber.length() != 16 || !cleanNumber.matches("\\d+")) {
            return false;
        }

        return luhnCheck(cleanNumber);
    }

    private boolean luhnCheck(String cardNumber) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return (sum % 10 == 0);
    }
}