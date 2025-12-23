package com.example.bankcards.exception;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends CustomException {

    public InsufficientFundsException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public InsufficientFundsException() {
        super("Insufficient funds for this operation", HttpStatus.BAD_REQUEST);
    }
}