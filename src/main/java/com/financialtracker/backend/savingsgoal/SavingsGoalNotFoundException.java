package com.financialtracker.backend.savingsgoal;

public class SavingsGoalNotFoundException extends RuntimeException {

    public SavingsGoalNotFoundException(Long id) {
        super("Savings goal with id " + id + " not found");
    }
}
