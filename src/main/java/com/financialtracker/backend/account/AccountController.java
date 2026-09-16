package com.financialtracker.backend.account;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts() {
        return accountService.getAllAccounts();
    }

    @PostMapping
    public AccountResponse createAccount(
            @Valid @RequestBody AccountRequest request) {

        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    @PutMapping("/{id}")
    public AccountResponse updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody AccountRequest request) {

        return accountService.updateAccount(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public AccountResponse deactivateAccount(@PathVariable Long id) {
        return accountService.deactivateAccount(id);
    }
}
