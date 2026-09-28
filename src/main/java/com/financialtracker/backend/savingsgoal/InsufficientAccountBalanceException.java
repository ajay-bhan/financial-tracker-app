package com.financialtracker.backend.savingsgoal;

public class InsufficientAccountBalanceException extends RuntimeException {

    public InsufficientAccountBalanceException(String message) {
        super(message);
    }
}
