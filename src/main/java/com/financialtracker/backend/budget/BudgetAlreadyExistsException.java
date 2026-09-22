package com.financialtracker.backend.budget;

public class BudgetAlreadyExistsException extends RuntimeException {

    public BudgetAlreadyExistsException(String category, java.time.YearMonth month) {
        super("A budget already exists for category '"
                + category
                + "' for month "
                + month);
    }
}
