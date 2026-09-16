package com.financialtracker.backend.account;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldCreateAccountSuccessfully() {
        // Arrange
        AccountRequest request = new AccountRequest();
        request.setName("Chase Checking");
        request.setAccountType(AccountType.CHECKING);
        request.setInstitutionName("Chase");
        request.setCurrentBalance(new BigDecimal("5000.00"));
        request.setCurrency("USD");
        request.setActive(true);

        Account savedAccount = new Account();
        savedAccount.setName("Chase Checking");
        savedAccount.setAccountType(AccountType.CHECKING);
        savedAccount.setInstitutionName("Chase");
        savedAccount.setCurrentBalance(new BigDecimal("5000.00"));
        savedAccount.setCurrency("USD");
        savedAccount.setActive(true);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(savedAccount);

        // Act
        AccountResponse response = accountService.createAccount(request);

        // Assert
        assertNotNull(response);
        assertEquals("Chase Checking", response.getName());
        assertEquals(AccountType.CHECKING, response.getAccountType());
        assertEquals("Chase", response.getInstitutionName());
        assertEquals(
                new BigDecimal("5000.00"),
                response.getCurrentBalance()
        );
        assertEquals("USD", response.getCurrency());
        assertTrue(response.isActive());

        verify(accountRepository).save(any(Account.class));
    }
    @Test
    void getAccountById_shouldReturnAccountSuccessfully() {
        // Arrange
        Long accountId = 1L;

        Account account = new Account();
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(new BigDecimal("5750.00"));
        account.setCurrency("USD");
        account.setActive(true);

        when(accountRepository.findById(accountId))
                .thenReturn(java.util.Optional.of(account));

        // Act
        AccountResponse response = accountService.getAccountById(accountId);

        // Assert
        assertNotNull(response);
        assertEquals("Chase Checking", response.getName());
        assertEquals(AccountType.CHECKING, response.getAccountType());
        assertEquals("Chase", response.getInstitutionName());
        assertEquals(
                (new BigDecimal("5750.00")),
                response.getCurrentBalance()
        );
        assertEquals("USD", response.getCurrency());
        assertTrue(response.isActive());

        verify(accountRepository).findById(accountId);
    }
    @Test
    void getAccountById_shouldThrowExceptionWhenAccountDoesNotExist() {
        // Arrange
        Long accountId = 999L;

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        // Act & Assert
        AccountNotFoundException exception = assertThrows(
                AccountNotFoundException.class,
                () -> accountService.getAccountById(accountId)
        );

        assertEquals(
                "Account with id 999 not found",
                exception.getMessage()
        );

        verify(accountRepository).findById(accountId);
    }
    @Test
    void updateAccount_shouldUpdateAccountSuccessfully() {
        // Arrange
        Long accountId = 1L;

        Account existingAccount = new Account();
        existingAccount.setName("Chase Checking");
        existingAccount.setAccountType(AccountType.CHECKING);
        existingAccount.setInstitutionName("Chase");
        existingAccount.setCurrentBalance(new BigDecimal("5000.00"));
        existingAccount.setCurrency("USD");
        existingAccount.setActive(true);

        AccountRequest request = new AccountRequest();
        request.setName("Chase Checking Updated");
        request.setAccountType(AccountType.CHECKING);
        request.setInstitutionName("Chase");
        request.setCurrentBalance(new BigDecimal("6000.00"));
        request.setCurrency("USD");
        request.setActive(true);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(existingAccount));

        when(accountRepository.save(existingAccount))
                .thenReturn(existingAccount);

        // Act
        AccountResponse response =
                accountService.updateAccount(accountId, request);

        // Assert
        assertNotNull(response);
        assertEquals("Chase Checking Updated", response.getName());
        assertEquals(AccountType.CHECKING, response.getAccountType());
        assertEquals("Chase", response.getInstitutionName());
        assertEquals(
                new BigDecimal("6000.00"),
                response.getCurrentBalance()
        );
        assertEquals("USD", response.getCurrency());
        assertTrue(response.isActive());

        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(existingAccount);
    }
    @Test
    void deactivateAccount_shouldSetAccountToInactive() {
        // Arrange
        Long accountId = 1L;

        Account account = new Account();
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(new BigDecimal("5750.00"));
        account.setCurrency("USD");
        account.setActive(true);

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(accountRepository.save(account))
                .thenReturn(account);

        // Act
        AccountResponse response =
                accountService.deactivateAccount(accountId);

        // Assert
        assertNotNull(response);
        assertFalse(response.isActive());
        assertEquals("Chase Checking", response.getName());
        assertEquals(
                (new BigDecimal("5750.00")),
                response.getCurrentBalance()
        );

        verify(accountRepository).findById(accountId);
        verify(accountRepository).save(account);
    }
    @Test
    void getAllAccounts_shouldReturnAllAccounts() {
        // Arrange
        Account account1 = new Account();
        account1.setName("Chase Checking");
        account1.setAccountType(AccountType.CHECKING);
        account1.setInstitutionName("Chase");
        account1.setCurrentBalance(new BigDecimal("5750.00"));
        account1.setCurrency("USD");
        account1.setActive(true);

        Account account2 = new Account();
        account2.setName("Chase Savings");
        account2.setAccountType(AccountType.SAVINGS);
        account2.setInstitutionName("Chase");
        account2.setCurrentBalance(new BigDecimal("10000.00"));
        account2.setCurrency("USD");
        account2.setActive(true);

        when(accountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        // Act
        List<AccountResponse> responses =
                accountService.getAllAccounts();

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());

        assertEquals("Chase Checking", responses.get(0).getName());
        assertEquals(AccountType.CHECKING, responses.get(0).getAccountType());

        assertEquals("Chase Savings", responses.get(1).getName());
        assertEquals(AccountType.SAVINGS, responses.get(1).getAccountType());

        verify(accountRepository).findAll();
    }
}
