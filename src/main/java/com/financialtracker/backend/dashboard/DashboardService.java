package com.financialtracker.backend.dashboard;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountRepository;
import com.financialtracker.backend.budget.Budget;
import com.financialtracker.backend.budget.BudgetRepository;
import com.financialtracker.backend.savingsgoal.SavingsGoal;
import com.financialtracker.backend.savingsgoal.SavingsGoalRepository;
import com.financialtracker.backend.transaction.TransactionRepository;
import com.financialtracker.backend.transaction.TransactionStatus;
import com.financialtracker.backend.transaction.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class DashboardService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final BudgetRepository budgetRepository;

    public DashboardService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            SavingsGoalRepository savingsGoalRepository,
            BudgetRepository budgetRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.savingsGoalRepository = savingsGoalRepository;
        this.budgetRepository = budgetRepository;
    }

    public DashboardResponse getDashboardSummary() {

        List<Account> activeAccounts =
                accountRepository.findByActiveTrue();

        BigDecimal totalBalance =
                activeAccounts.stream()
                        .map(Account::getCurrentBalance)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalIncome =
                transactionRepository.findByTransactionTypeAndStatus(
                                TransactionType.INCOME,
                                TransactionStatus.ACTIVE
                        ).stream()
                        .map(transaction -> transaction.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses =
                transactionRepository.findByTransactionTypeAndStatus(
                                TransactionType.EXPENSE,
                                TransactionStatus.ACTIVE
                        ).stream()
                        .map(transaction -> transaction.getAmount())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal netCashFlow =
                totalIncome.subtract(totalExpenses);

        // Savings goal progress
        List<SavingsGoal> activeSavingsGoals =
                savingsGoalRepository.findByActiveTrue();

        BigDecimal totalSavingsTarget =
                activeSavingsGoals.stream()
                        .map(SavingsGoal::getTargetAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSavingsCurrent =
                activeSavingsGoals.stream()
                        .map(SavingsGoal::getCurrentAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal savingsGoalProgress =
                BigDecimal.ZERO;

        if (totalSavingsTarget.compareTo(BigDecimal.ZERO) > 0) {
            savingsGoalProgress =
                    totalSavingsCurrent
                            .multiply(new BigDecimal("100"))
                            .divide(
                                    totalSavingsTarget,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

// Budget usage
        List<Budget> activeBudgets =
                budgetRepository.findByActiveTrue();

        BigDecimal totalBudgetLimit =
                activeBudgets.stream()
                        .map(Budget::getMonthlyLimit)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBudgetSpent = BigDecimal.ZERO;

        for (Budget budget : activeBudgets) {

            LocalDate startDate =
                    budget.getMonth().atDay(1);

            LocalDate endDate =
                    budget.getMonth().atEndOfMonth();

            BigDecimal categorySpent =
                    transactionRepository
                            .findByTransactionTypeAndStatusAndCategoryAndTransactionDateBetween(
                                    TransactionType.EXPENSE,
                                    TransactionStatus.ACTIVE,
                                    budget.getCategory(),
                                    startDate,
                                    endDate
                            )
                            .stream()
                            .map(transaction -> transaction.getAmount())
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalBudgetSpent =
                    totalBudgetSpent.add(categorySpent);
        }

        BigDecimal budgetUsagePercentage =
                BigDecimal.ZERO;

        if (totalBudgetLimit.compareTo(BigDecimal.ZERO) > 0) {

            budgetUsagePercentage =
                    totalBudgetSpent
                            .multiply(new BigDecimal("100"))
                            .divide(
                                    totalBudgetLimit,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        DashboardResponse response =
                new DashboardResponse();

        response.setTotalBalance(totalBalance);
        response.setTotalIncome(totalIncome);
        response.setTotalExpenses(totalExpenses);
        response.setNetCashFlow(netCashFlow);
        response.setSavingsGoalProgress(savingsGoalProgress);
        response.setBudgetUsagePercentage(budgetUsagePercentage);

        return response;
    }
}
