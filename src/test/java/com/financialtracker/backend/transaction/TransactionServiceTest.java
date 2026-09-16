package com.financialtracker.backend.transaction;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountRepository;
import com.financialtracker.backend.account.AccountType;
import com.financialtracker.backend.account.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_shouldCreateExpenseSuccessfully() {

        Account account = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransactionType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("125.50"));
        request.setCategory("Groceries");
        request.setDescription("Weekly groceries");
        request.setTransactionDate(LocalDate.of(2026, 9, 11));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(1L);
        savedTransaction.setAccount(account);
        savedTransaction.setTransactionType(TransactionType.EXPENSE);
        savedTransaction.setAmount(new BigDecimal("125.50"));
        savedTransaction.setCategory("Groceries");
        savedTransaction.setDescription("Weekly groceries");
        savedTransaction.setTransactionDate(
                LocalDate.of(2026, 9, 11)
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5L, response.getAccountId());
        assertEquals(
                TransactionType.EXPENSE,
                response.getTransactionType()
        );
        assertEquals(
                new BigDecimal("125.50"),
                response.getAmount()
        );

        assertEquals(
                new BigDecimal("4874.50"),
                account.getCurrentBalance()
        );

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldCreateIncomeSuccessfully() {

        Account account = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransactionType(TransactionType.INCOME);
        request.setAmount(new BigDecimal("1000.00"));
        request.setCategory("Salary");
        request.setDescription("Monthly salary");
        request.setTransactionDate(LocalDate.of(2026, 9, 11));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(2L);
        savedTransaction.setAccount(account);
        savedTransaction.setTransactionType(TransactionType.INCOME);
        savedTransaction.setAmount(new BigDecimal("1000.00"));
        savedTransaction.setCategory("Salary");
        savedTransaction.setDescription("Monthly salary");
        savedTransaction.setTransactionDate(
                LocalDate.of(2026, 9, 11)
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response);
        assertEquals(
                TransactionType.INCOME,
                response.getTransactionType()
        );

        assertEquals(
                new BigDecimal("6000.00"),
                account.getCurrentBalance()
        );

        verify(accountRepository).save(account);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldThrowExceptionWhenAccountDoesNotExist() {

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(999L);
        request.setTransactionType(TransactionType.EXPENSE);
        request.setAmount(new BigDecimal("100.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 11));

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.createTransaction(request)
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void getTransactionById_shouldReturnTransactionSuccessfully() {

        Account account = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setAmount(new BigDecimal("125.50"));
        transaction.setCategory("Groceries");
        transaction.setDescription("Weekly groceries");
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 11)
        );

        when(transactionRepository.findById(1L))
                .thenReturn(Optional.of(transaction));

        TransactionResponse response =
                transactionService.getTransactionById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(5L, response.getAccountId());
        assertEquals(
                TransactionType.EXPENSE,
                response.getTransactionType()
        );
        assertEquals(
                new BigDecimal("125.50"),
                response.getAmount()
        );
    }

    @Test
    void getTransactionById_shouldThrowExceptionWhenTransactionDoesNotExist() {

        when(transactionRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(999L)
        );
    }
    @Test
    void createTransaction_shouldTransferMoneyBetweenAccounts() {

        Account sourceAccount = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        Account destinationAccount = createAccount(
                6L,
                new BigDecimal("2000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransferAccountId(6L);
        request.setTransactionType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setDescription("Transfer to savings");
        request.setTransactionDate(LocalDate.of(2026, 9, 12));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findById(6L))
                .thenReturn(Optional.of(destinationAccount));

        Transaction savedTransaction = new Transaction();
        savedTransaction.setId(3L);
        savedTransaction.setAccount(sourceAccount);
        savedTransaction.setTransferAccount(destinationAccount);
        savedTransaction.setTransactionType(TransactionType.TRANSFER);
        savedTransaction.setAmount(new BigDecimal("1000.00"));
        savedTransaction.setDescription("Transfer to savings");
        savedTransaction.setTransactionDate(
                LocalDate.of(2026, 9, 12)
        );

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(savedTransaction);

        TransactionResponse response =
                transactionService.createTransaction(request);

        assertNotNull(response);

        assertEquals(3L, response.getId());
        assertEquals(5L, response.getAccountId());
        assertEquals(6L, response.getTransferAccountId());
        assertEquals(
                TransactionType.TRANSFER,
                response.getTransactionType()
        );
        assertEquals(
                new BigDecimal("1000.00"),
                response.getAmount()
        );

        assertEquals(
                new BigDecimal("4000.00"),
                sourceAccount.getCurrentBalance()
        );

        assertEquals(
                new BigDecimal("3000.00"),
                destinationAccount.getCurrentBalance()
        );

        verify(accountRepository).save(sourceAccount);
        verify(accountRepository).save(destinationAccount);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldRejectTransferWithoutDestinationAccount() {

        Account sourceAccount = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransactionType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 12));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals(
                "transferAccountId is required for transfers",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldRejectTransferToNonexistentAccount() {

        Account sourceAccount = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransferAccountId(999L);
        request.setTransactionType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 12));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AccountNotFoundException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals(
                new BigDecimal("5000.00"),
                sourceAccount.getCurrentBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void createTransaction_shouldRejectTransferToSameAccount() {

        Account account = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        TransactionRequest request = new TransactionRequest();
        request.setAccountId(5L);
        request.setTransferAccountId(5L);
        request.setTransactionType(TransactionType.TRANSFER);
        request.setAmount(new BigDecimal("1000.00"));
        request.setTransactionDate(LocalDate.of(2026, 9, 12));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals(
                "Source and destination accounts must be different",
                exception.getMessage()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                account.getCurrentBalance()
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void reverseTransaction_shouldReverseExpenseSuccessfully() {

        Account account = createAccount(
                5L,
                new BigDecimal("4874.50")
        );

        Transaction transaction = new Transaction();
        transaction.setId(10L);
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setAmount(new BigDecimal("125.50"));
        transaction.setCategory("Groceries");
        transaction.setDescription("Weekly groceries");
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 11)
        );

        when(transactionRepository.findById(10L))
                .thenReturn(Optional.of(transaction));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponse response =
                transactionService.reverseTransaction(10L);

        assertNotNull(response);

        assertEquals(
                TransactionStatus.REVERSED,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                account.getCurrentBalance()
        );

        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void reverseTransaction_shouldReverseIncomeSuccessfully() {

        Account account = createAccount(
                5L,
                new BigDecimal("6000.00")
        );

        Transaction transaction = new Transaction();
        transaction.setId(11L);
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.INCOME);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setCategory("Salary");
        transaction.setDescription("Monthly salary");
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 11)
        );

        when(transactionRepository.findById(11L))
                .thenReturn(Optional.of(transaction));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponse response =
                transactionService.reverseTransaction(11L);

        assertNotNull(response);

        assertEquals(
                TransactionStatus.REVERSED,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                account.getCurrentBalance()
        );

        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void reverseTransaction_shouldReverseTransferSuccessfully() {

        Account sourceAccount = createAccount(
                5L,
                new BigDecimal("4000.00")
        );

        Account destinationAccount = createAccount(
                6L,
                new BigDecimal("3000.00")
        );

        Transaction transaction = new Transaction();
        transaction.setId(12L);
        transaction.setAccount(sourceAccount);
        transaction.setTransferAccount(destinationAccount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setAmount(new BigDecimal("1000.00"));
        transaction.setDescription("Transfer to savings");
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 12)
        );

        when(transactionRepository.findById(12L))
                .thenReturn(Optional.of(transaction));

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        TransactionResponse response =
                transactionService.reverseTransaction(12L);

        assertNotNull(response);

        assertEquals(
                TransactionStatus.REVERSED,
                response.getStatus()
        );

        assertEquals(
                new BigDecimal("5000.00"),
                sourceAccount.getCurrentBalance()
        );

        assertEquals(
                new BigDecimal("2000.00"),
                destinationAccount.getCurrentBalance()
        );

        verify(accountRepository).save(sourceAccount);
        verify(accountRepository).save(destinationAccount);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void reverseTransaction_shouldRejectAlreadyReversedTransaction() {

        Account account = createAccount(
                5L,
                new BigDecimal("5000.00")
        );

        Transaction transaction = new Transaction();
        transaction.setId(13L);
        transaction.setAccount(account);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setStatus(TransactionStatus.REVERSED);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setDescription("Already reversed expense");
        transaction.setTransactionDate(
                LocalDate.of(2026, 9, 13)
        );

        when(transactionRepository.findById(13L))
                .thenReturn(Optional.of(transaction));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> transactionService.reverseTransaction(13L)
        );

        assertEquals(
                "Transaction with id 13 has already been reversed",
                exception.getMessage()
        );

        // Balance must not change
        assertEquals(
                new BigDecimal("5000.00"),
                account.getCurrentBalance()
        );

        // Nothing should be saved because reversal was rejected
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    private Account createAccount(Long id, BigDecimal balance) {

        Account account = new Account();

        account.setId(id);
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(balance);
        account.setCurrency("USD");
        account.setActive(true);

        return account;
    }
}
