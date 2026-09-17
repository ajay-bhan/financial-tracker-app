package com.financialtracker.backend.budget;

public class BudgetNotFoundException extends RuntimeException {

    public BudgetNotFoundException(Long id) {
        super("Budget with id " + id + " not found");
    }
}
