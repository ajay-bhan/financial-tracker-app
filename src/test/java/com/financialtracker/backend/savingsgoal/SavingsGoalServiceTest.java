package com.financialtracker.backend.savingsgoal;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountRepository;
import com.financialtracker.backend.account.AccountType;
import com.financialtracker.backend.transaction.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.financialtracker.backend.transaction.Transaction;
import com.financialtracker.backend.transaction.TransactionStatus;
import com.financialtracker.backend.transaction.TransactionType;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private SavingsGoalService savingsGoalService;

    private SavingsGoalRequest request;

    private java.time.Clock clock;

    @BeforeEach
    void setUp() {

        clock = java.time.Clock.fixed(
                java.time.Instant.parse("2026-09-24T12:00:00Z"),
                java.time.ZoneOffset.UTC
        );

        savingsGoalService =
                new SavingsGoalService(
                        savingsGoalRepository,
                        accountRepository,
                        clock,
                        transactionRepository
                );

        request = new SavingsGoalRequest();
        request.setName("Emergency Fund");
        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setCurrentAmount(new BigDecimal("3500.00"));
        request.setTargetDate(
                java.time.LocalDate.of(2027, 12, 31)
        );
    }

    @Test
    void createSavingsGoal_shouldCreateGoalSuccessfully() {

        SavingsGoal savedGoal = new SavingsGoal();
        savedGoal.setId(1L);
        savedGoal.setName("Emergency Fund");
        savedGoal.setTargetAmount(new BigDecimal("10000.00"));
        savedGoal.setCurrentAmount(new BigDecimal("3500.00"));
        savedGoal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        savedGoal.setActive(true);

        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenReturn(savedGoal);

        SavingsGoalResponse response =
                savingsGoalService.createSavingsGoal(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Emergency Fund", response.getName());
        assertEquals(
                new BigDecimal("10000.00"),
                response.getTargetAmount()
        );
        assertEquals(
                new BigDecimal("3500.00"),
                response.getCurrentAmount()
        );
        assertEquals(
                new BigDecimal("6500.00"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("35.00"),
                response.getProgressPercentage()
        );
        assertEquals(
                java.time.LocalDate.of(2027, 12, 31),
                response.getTargetDate()
        );
        assertTrue(response.isActive());

        verify(savingsGoalRepository).save(any(SavingsGoal.class));
    }
    @Test
    void getAllSavingsGoals_shouldReturnAllGoals() {

        SavingsGoal goal1 = new SavingsGoal();
        goal1.setId(1L);
        goal1.setName("Emergency Fund");
        goal1.setTargetAmount(new BigDecimal("10000.00"));
        goal1.setCurrentAmount(new BigDecimal("3500.00"));
        goal1.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        goal1.setActive(true);

        SavingsGoal goal2 = new SavingsGoal();
        goal2.setId(2L);
        goal2.setName("Vacation Fund");
        goal2.setTargetAmount(new BigDecimal("5000.00"));
        goal2.setCurrentAmount(new BigDecimal("1000.00"));
        goal2.setTargetDate(java.time.LocalDate.of(2027, 6, 30));
        goal2.setActive(true);

        when(savingsGoalRepository.findAll())
                .thenReturn(java.util.List.of(goal1, goal2));

        java.util.List<SavingsGoalResponse> responses =
                savingsGoalService.getAllSavingsGoals();

        assertEquals(2, responses.size());

        assertEquals(1L, responses.get(0).getId());
        assertEquals("Emergency Fund", responses.get(0).getName());

        assertEquals(2L, responses.get(1).getId());
        assertEquals("Vacation Fund", responses.get(1).getName());

        verify(savingsGoalRepository).findAll();
    }
    @Test
    void getSavingsGoalById_shouldReturnGoalSuccessfully() {

        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setCurrentAmount(new BigDecimal("3500.00"));
        goal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        SavingsGoalResponse response =
                savingsGoalService.getSavingsGoalById(goalId);

        assertNotNull(response);
        assertEquals(goalId, response.getId());
        assertEquals("Emergency Fund", response.getName());
        assertEquals(
                new BigDecimal("10000.00"),
                response.getTargetAmount()
        );
        assertEquals(
                new BigDecimal("3500.00"),
                response.getCurrentAmount()
        );
        assertEquals(
                java.time.LocalDate.of(2027, 12, 31),
                response.getTargetDate()
        );
        assertTrue(response.isActive());

        verify(savingsGoalRepository).findById(goalId);
    }
    @Test
    void getSavingsGoalById_shouldThrowExceptionWhenGoalDoesNotExist() {

        Long goalId = 999L;

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                SavingsGoalNotFoundException.class,
                () -> savingsGoalService.getSavingsGoalById(goalId)
        );

        verify(savingsGoalRepository).findById(goalId);
    }
    @Test
    void updateSavingsGoal_shouldUpdateGoalSuccessfully() {

        Long goalId = 1L;

        SavingsGoal existingGoal = new SavingsGoal();
        existingGoal.setId(goalId);
        existingGoal.setName("Emergency Fund");
        existingGoal.setTargetAmount(new BigDecimal("10000.00"));
        existingGoal.setCurrentAmount(new BigDecimal("3500.00"));
        existingGoal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        existingGoal.setActive(true);

        SavingsGoalRequest updateRequest = new SavingsGoalRequest();
        updateRequest.setName("Emergency Fund");
        updateRequest.setTargetAmount(new BigDecimal("12000.00"));
        updateRequest.setCurrentAmount(new BigDecimal("4000.00"));
        updateRequest.setTargetDate(java.time.LocalDate.of(2027, 12, 31));

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(existingGoal));

        when(savingsGoalRepository.save(existingGoal))
                .thenReturn(existingGoal);

        SavingsGoalResponse response =
                savingsGoalService.updateSavingsGoal(
                        goalId,
                        updateRequest
                );

        assertNotNull(response);
        assertEquals(goalId, response.getId());
        assertEquals("Emergency Fund", response.getName());
        assertEquals(
                new BigDecimal("12000.00"),
                response.getTargetAmount()
        );
        assertEquals(
                new BigDecimal("4000.00"),
                response.getCurrentAmount()
        );
        assertEquals(
                java.time.LocalDate.of(2027, 12, 31),
                response.getTargetDate()
        );
        assertTrue(response.isActive());

        verify(savingsGoalRepository).findById(goalId);
        verify(savingsGoalRepository).save(existingGoal);
    }
    @Test
    void updateSavingsGoal_shouldThrowExceptionWhenGoalDoesNotExist() {

        Long goalId = 999L;

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                SavingsGoalNotFoundException.class,
                () -> savingsGoalService.updateSavingsGoal(
                        goalId,
                        request
                )
        );

        verify(savingsGoalRepository).findById(goalId);
        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }
    @Test
    void deactivateSavingsGoal_shouldDeactivateGoalSuccessfully() {

        Long goalId = 1L;

        SavingsGoal existingGoal = new SavingsGoal();
        existingGoal.setId(goalId);
        existingGoal.setName("Emergency Fund");
        existingGoal.setTargetAmount(new BigDecimal("12000.00"));
        existingGoal.setCurrentAmount(new BigDecimal("4000.00"));
        existingGoal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        existingGoal.setActive(true);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(existingGoal));

        when(savingsGoalRepository.save(existingGoal))
                .thenReturn(existingGoal);

        SavingsGoalResponse response =
                savingsGoalService.deactivateSavingsGoal(goalId);

        assertNotNull(response);
        assertEquals(goalId, response.getId());
        assertEquals("Emergency Fund", response.getName());
        assertEquals(
                new BigDecimal("12000.00"),
                response.getTargetAmount()
        );
        assertEquals(
                new BigDecimal("4000.00"),
                response.getCurrentAmount()
        );
        assertFalse(response.isActive());

        verify(savingsGoalRepository).findById(goalId);
        verify(savingsGoalRepository).save(existingGoal);
    }
    @Test
    void deactivateSavingsGoal_shouldThrowExceptionWhenGoalDoesNotExist() {

        Long goalId = 999L;

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.empty());

        assertThrows(
                SavingsGoalNotFoundException.class,
                () -> savingsGoalService.deactivateSavingsGoal(goalId)
        );

        verify(savingsGoalRepository).findById(goalId);
        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }

    @Test
    void getSavingsGoalById_shouldCapProgressAndRemainingAmount() {

        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("10000.00"));
        goal.setCurrentAmount(new BigDecimal("12000.00"));
        goal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        SavingsGoalResponse response =
                savingsGoalService.getSavingsGoalById(goalId);

        assertEquals(
                new BigDecimal("0"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("100.00"),
                response.getProgressPercentage()
        );

        verify(savingsGoalRepository).findById(goalId);
    }
    @Test
    void getSavingsGoalById_shouldCalculateRequiredMonthlySavings() {
        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(java.time.LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        SavingsGoalResponse response =
                savingsGoalService.getSavingsGoalById(goalId);

        assertEquals(
                new BigDecimal("500.00"),
                response.getRequiredMonthlySavings()
        );

        verify(savingsGoalRepository).findById(goalId);
    }
    @Test
    void createSavingsGoal_shouldThrowExceptionWhenCurrentAmountExceedsTargetAmount() {

        request.setTargetAmount(new BigDecimal("10000.00"));
        request.setCurrentAmount(new BigDecimal("15000.00"));

        assertThrows(
                InvalidSavingsGoalException.class,
                () -> savingsGoalService.createSavingsGoal(request)
        );

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }
    @Test
    void updateSavingsGoal_shouldThrowExceptionWhenCurrentAmountExceedsTargetAmount() {

        Long goalId = 1L;

        SavingsGoalRequest updateRequest = new SavingsGoalRequest();
        updateRequest.setName("Emergency Fund");
        updateRequest.setTargetAmount(new BigDecimal("10000.00"));
        updateRequest.setCurrentAmount(new BigDecimal("15000.00"));
        updateRequest.setTargetDate(
                java.time.LocalDate.of(2027, 12, 31)
        );

        assertThrows(
                InvalidSavingsGoalException.class,
                () -> savingsGoalService.updateSavingsGoal(
                        goalId,
                        updateRequest
                )
        );

        verify(savingsGoalRepository, never())
                .findById(goalId);

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }
    @Test
    void contributeToSavingsGoal_shouldIncreaseCurrentAmountSuccessfully() {

        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(
                java.time.LocalDate.of(2027, 12, 31)
        );
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCurrency("USD");
        account.setActive(true);

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();

        contributionRequest.setAmount(
                new BigDecimal("500.00")
        );

        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(java.util.Optional.of(account));

        when(savingsGoalRepository.save(goal))
                .thenReturn(goal);

        SavingsGoalResponse response =
                savingsGoalService.contributeToSavingsGoal(
                        goalId,
                        contributionRequest
                );

        assertEquals(
                new BigDecimal("4500.00"),
                response.getCurrentAmount()
        );

        assertEquals(
                new BigDecimal("7500.00"),
                response.getRemainingAmount()
        );

        assertEquals(
                new BigDecimal("37.50"),
                response.getProgressPercentage()
        );
        assertEquals(
                new BigDecimal("4500.00"),
                account.getCurrentBalance()
        );

        verify(savingsGoalRepository).findById(goalId);
        verify(accountRepository).findById(5L);
        verify(accountRepository).save(account);
        verify(savingsGoalRepository).save(goal);
    }


    @Test
    void contributeToSavingsGoal_shouldThrowExceptionWhenContributionExceedsTarget() {

        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("9500.00"));
        goal.setTargetDate(
                java.time.LocalDate.of(2027, 12, 31)
        );
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(new BigDecimal("5000.00"));
        account.setCurrency("USD");
        account.setActive(true);

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();

        contributionRequest.setAmount(
                new BigDecimal("3000.00")
        );

        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(java.util.Optional.of(account));

        assertThrows(
                InvalidSavingsGoalException.class,
                () -> savingsGoalService.contributeToSavingsGoal(
                        goalId,
                        contributionRequest
                )
        );

        assertEquals(
                new BigDecimal("9500.00"),
                goal.getCurrentAmount()
        );

        verify(savingsGoalRepository).findById(goalId);
        verify(accountRepository).findById(5L);

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }

    @Test
    void contributeToSavingsGoal_shouldThrowExceptionWhenAccountBalanceIsInsufficient() {

        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(
                java.time.LocalDate.of(2027, 12, 31)
        );
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setName("Chase Checking");
        account.setAccountType(AccountType.CHECKING);
        account.setInstitutionName("Chase");
        account.setCurrentBalance(new BigDecimal("100.00"));
        account.setCurrency("USD");
        account.setActive(true);

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();

        contributionRequest.setAmount(
                new BigDecimal("500.00")
        );

        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(java.util.Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(java.util.Optional.of(account));

        assertThrows(
                InsufficientAccountBalanceException.class,
                () -> savingsGoalService.contributeToSavingsGoal(
                        goalId,
                        contributionRequest
                )
        );

        assertEquals(
                new BigDecimal("4000.00"),
                goal.getCurrentAmount()
        );

        assertEquals(
                new BigDecimal("100.00"),
                account.getCurrentBalance()
        );

        verify(savingsGoalRepository).findById(goalId);
        verify(accountRepository).findById(5L);

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }

    @Test
    void contributeToSavingsGoalShouldCreateTransaction() {
        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("4000.00"));
        goal.setTargetDate(LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setCurrentBalance(new BigDecimal("5000.00"));

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();
        contributionRequest.setAmount(new BigDecimal("500.00"));
        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        when(savingsGoalRepository.save(any(SavingsGoal.class)))
                .thenReturn(goal);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        Transaction transaction = new Transaction();
        transaction.setId(1L);

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);

        savingsGoalService.contributeToSavingsGoal(
                goalId,
                contributionRequest
        );

        ArgumentCaptor<Transaction> transactionCaptor =
                ArgumentCaptor.forClass(Transaction.class);

        verify(transactionRepository).save(transactionCaptor.capture());

        Transaction savedTransaction = transactionCaptor.getValue();

        assertEquals(TransactionType.EXPENSE, savedTransaction.getTransactionType());
        assertEquals(TransactionStatus.ACTIVE, savedTransaction.getStatus());
        assertEquals(new BigDecimal("500.00"), savedTransaction.getAmount());
        assertEquals("Savings", savedTransaction.getCategory());
        assertEquals("Contribution to Emergency Fund",
                savedTransaction.getDescription());
        assertEquals(account, savedTransaction.getAccount());
        assertNull(savedTransaction.getTransferAccount());
        assertEquals(
                LocalDate.of(2026, 9, 24),
                savedTransaction.getTransactionDate()
        );

        assertEquals(
                LocalDateTime.of(2026, 9, 24, 12, 0),
                savedTransaction.getCreatedAt()
        );
    }

    @Test
    void contributeToSavingsGoalShouldNotCreateTransactionWhenContributionExceedsTarget() {
        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("11900.00"));
        goal.setTargetDate(LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setCurrentBalance(new BigDecimal("5000.00"));

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();
        contributionRequest.setAmount(new BigDecimal("200.00"));
        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InvalidSavingsGoalException.class,
                () -> savingsGoalService.contributeToSavingsGoal(
                        goalId,
                        contributionRequest
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }

    @Test
    void contributeToSavingsGoalShouldNotCreateTransactionWhenAccountBalanceIsInsufficient() {
        Long goalId = 1L;

        SavingsGoal goal = new SavingsGoal();
        goal.setId(goalId);
        goal.setName("Emergency Fund");
        goal.setTargetAmount(new BigDecimal("12000.00"));
        goal.setCurrentAmount(new BigDecimal("10000.00"));
        goal.setTargetDate(LocalDate.of(2027, 12, 31));
        goal.setActive(true);

        Account account = new Account();
        account.setId(5L);
        account.setCurrentBalance(new BigDecimal("50.00"));

        SavingsContributionRequest contributionRequest =
                new SavingsContributionRequest();
        contributionRequest.setAmount(new BigDecimal("100.00"));
        contributionRequest.setAccountId(5L);

        when(savingsGoalRepository.findById(goalId))
                .thenReturn(Optional.of(goal));

        when(accountRepository.findById(5L))
                .thenReturn(Optional.of(account));

        assertThrows(
                InsufficientAccountBalanceException.class,
                () -> savingsGoalService.contributeToSavingsGoal(
                        goalId,
                        contributionRequest
                )
        );

        verify(transactionRepository, never())
                .save(any(Transaction.class));

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(savingsGoalRepository, never())
                .save(any(SavingsGoal.class));
    }

}
