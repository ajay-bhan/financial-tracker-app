package com.financialtracker.backend.account;

public enum AccountType {
    CHECKING,
    SAVINGS,
    CREDIT_CARD,
    INVESTMENT,
    CASH,
    LOAN,
    OTHER;

    public static String[] valuesAsString() {
        return java.util.Arrays.stream(values())
                .map(Enum::name)
                .toArray(String[]::new);
    }
}
