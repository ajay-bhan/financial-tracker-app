package com.financialtracker.backend.account;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    public AccountResponse createAccount(AccountRequest request) {
        Account account = toAccount(request);

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }

    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        return toAccountResponse(account);
    }

    public AccountResponse updateAccount(Long id, AccountRequest request) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        existingAccount.setName(request.getName());
        existingAccount.setAccountType(request.getAccountType());
        existingAccount.setInstitutionName(request.getInstitutionName());
        existingAccount.setCurrentBalance(request.getCurrentBalance());
        existingAccount.setCurrency(request.getCurrency());
        existingAccount.setActive(request.getActive());

        Account savedAccount = accountRepository.save(existingAccount);

        return toAccountResponse(savedAccount);
    }

    public AccountResponse deactivateAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setActive(false);

        Account savedAccount = accountRepository.save(account);

        return toAccountResponse(savedAccount);
    }
    private AccountResponse toAccountResponse(Account account) {
        AccountResponse response = new AccountResponse();

        response.setId(account.getId());
        response.setName(account.getName());
        response.setAccountType(account.getAccountType());
        response.setInstitutionName(account.getInstitutionName());
        response.setCurrentBalance(account.getCurrentBalance());
        response.setCurrency(account.getCurrency());
        response.setActive(account.isActive());

        return response;
    }
    private Account toAccount(AccountRequest request) {
        Account account = new Account();

        account.setName(request.getName());
        account.setAccountType(request.getAccountType());
        account.setInstitutionName(request.getInstitutionName());
        account.setCurrentBalance(request.getCurrentBalance());
        account.setCurrency(request.getCurrency());
        account.setActive(request.getActive());

        return account;
    }
}
