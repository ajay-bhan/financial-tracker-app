package com.financialtracker.backend.savingsgoal;

public class InvalidSavingsGoalException extends RuntimeException {

    public InvalidSavingsGoalException(String message) {
        super(message);
    }
}
