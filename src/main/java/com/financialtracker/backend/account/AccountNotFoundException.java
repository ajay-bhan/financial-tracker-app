package com.financialtracker.backend.account;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Account with id " + id + " not found");
    }
}
