package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Component
public class DateUtil {

    private static final DateTimeFormatter EXPIRY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    /**
     * Проверяет, не истек ли срок действия карты
     */
    public boolean isExpired(LocalDate expiryDate) {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Генерирует дату истечения на N лет вперед
     */
    public LocalDate generateExpiryDate(int yearsFromNow) {
        return LocalDate.now().plusYears(yearsFromNow);
    }

    /**
     * Форматирует дату истечения в формат MM/YY
     */
    public String formatExpiryDate(LocalDate expiryDate) {
        if (expiryDate == null) {
            return "";
        }
        YearMonth yearMonth = YearMonth.from(expiryDate);
        return yearMonth.format(EXPIRY_DATE_FORMATTER);
    }

    /**
     * Парсит дату истечения из формата MM/YY
     */
    public LocalDate parseExpiryDate(String expiryDateStr) {
        try {
            YearMonth yearMonth = YearMonth.parse(expiryDateStr, EXPIRY_DATE_FORMATTER);
            return yearMonth.atEndOfMonth();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid expiry date format. Expected MM/yy");
        }
    }

    /**
     * Проверяет, валидна ли дата истечения (не в прошлом)
     */
    public boolean isValidExpiryDate(LocalDate expiryDate) {
        return expiryDate != null && !expiryDate.isBefore(LocalDate.now());
    }
}