package com.financialtracker.backend.dashboard;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountRepository;
import com.financialtracker.backend.account.AccountType;
import com.financialtracker.backend.budget.Budget;
import com.financialtracker.backend.budget.BudgetRepository;
import com.financialtracker.backend.savingsgoal.SavingsGoal;
import com.financialtracker.backend.savingsgoal.SavingsGoalRepository;
import com.financialtracker.backend.transaction.Transaction;
import com.financialtracker.backend.transaction.TransactionRepository;
import com.financialtracker.backend.transaction.TransactionStatus;
import com.financialtracker.backend.transaction.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SavingsGoalRepository savingsGoalRepository;

    @Mock
    private BudgetRepository budgetRepository;

    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {

        dashboardService =
                new DashboardService(
                        accountRepository,
                        transactionRepository,
                        savingsGoalRepository,
                        budgetRepository
                );
    }

    @Test
    void getDashboardSummaryShouldCalculateTotalBalance() {

        Account checking = new Account();
        checking.setCurrentBalance(new BigDecimal("4000.00"));
        checking.setAccountType(AccountType.CHECKING);

        Account savings = new Account();
        savings.setCurrentBalance(new BigDecimal("2500.00"));
        savings.setAccountType(AccountType.SAVINGS);

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of(checking, savings));

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("6500.00"),
                response.getTotalBalance()
        );

        assertEquals(
                new BigDecimal("0"),
                response.getTotalIncome()
        );

        assertEquals(
                new BigDecimal("0"),
                response.getTotalExpenses()
        );

        assertEquals(
                new BigDecimal("0"),
                response.getNetCashFlow()
        );
    }

    @Test
    void getDashboardSummaryShouldIgnoreInactiveAccounts() {

        Account activeAccount = new Account();
        activeAccount.setCurrentBalance(new BigDecimal("4000.00"));
        activeAccount.setAccountType(AccountType.CHECKING);

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of(activeAccount));

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("4000.00"),
                response.getTotalBalance()
        );
    }

    @Test
    void getDashboardSummaryShouldReturnZeroWhenNoActiveAccountsExist() {

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("0"),
                response.getTotalBalance()
        );
    }

    @Test
    void getDashboardSummaryShouldCalculateTotalIncome() {

        Transaction income1 = new Transaction();
        income1.setAmount(new BigDecimal("1000.00"));
        income1.setTransactionType(TransactionType.INCOME);
        income1.setStatus(TransactionStatus.ACTIVE);

        Transaction income2 = new Transaction();
        income2.setAmount(new BigDecimal("500.00"));
        income2.setTransactionType(TransactionType.INCOME);
        income2.setStatus(TransactionStatus.ACTIVE);

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of(income1, income2));

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("1500.00"),
                response.getTotalIncome()
        );
    }

    @Test
    void getDashboardSummaryShouldCalculateNetCashFlow() {

        Transaction income = new Transaction();
        income.setAmount(new BigDecimal("2000.00"));
        income.setTransactionType(TransactionType.INCOME);
        income.setStatus(TransactionStatus.ACTIVE);

        Transaction expense = new Transaction();
        expense.setAmount(new BigDecimal("750.00"));
        expense.setTransactionType(TransactionType.EXPENSE);
        expense.setStatus(TransactionStatus.ACTIVE);

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of(income));

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of(expense));

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("1250.00"),
                response.getNetCashFlow()
        );
    }

    @Test
    void getDashboardSummaryShouldCalculateSavingsGoalProgress() {

        SavingsGoal emergencyFund = new SavingsGoal();
        emergencyFund.setTargetAmount(new BigDecimal("12000.00"));
        emergencyFund.setCurrentAmount(new BigDecimal("10101.00"));

        SavingsGoal vacation = new SavingsGoal();
        vacation.setTargetAmount(new BigDecimal("5000.00"));
        vacation.setCurrentAmount(new BigDecimal("2000.00"));

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of(
                        emergencyFund,
                        vacation
                ));

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("71.18"),
                response.getSavingsGoalProgress()
        );
    }

    @Test
    void getDashboardSummaryShouldCalculateBudgetUsagePercentage() {

        Budget foodBudget = new Budget();
        foodBudget.setMonthlyLimit(new BigDecimal("600.00"));

        Budget transportationBudget = new Budget();
        transportationBudget.setMonthlyLimit(new BigDecimal("350.00"));

        Transaction expense = new Transaction();
        expense.setAmount(new BigDecimal("617.50"));
        expense.setTransactionType(TransactionType.EXPENSE);
        expense.setStatus(TransactionStatus.ACTIVE);

        when(accountRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.INCOME,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of());

        when(transactionRepository.findByTransactionTypeAndStatus(
                TransactionType.EXPENSE,
                TransactionStatus.ACTIVE
        )).thenReturn(List.of(expense));

        when(savingsGoalRepository.findByActiveTrue())
                .thenReturn(List.of());

        when(budgetRepository.findByActiveTrue())
                .thenReturn(List.of(
                        foodBudget,
                        transportationBudget
                ));

        DashboardResponse response =
                dashboardService.getDashboardSummary();

        assertEquals(
                new BigDecimal("65.00"),
                response.getBudgetUsagePercentage()
        );
    }
}
