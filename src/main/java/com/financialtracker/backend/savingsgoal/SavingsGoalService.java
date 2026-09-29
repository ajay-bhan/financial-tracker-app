package com.financialtracker.backend.savingsgoal;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountNotFoundException;
import com.financialtracker.backend.account.AccountRepository;
import com.financialtracker.backend.transaction.Transaction;
import com.financialtracker.backend.transaction.TransactionRepository;
import com.financialtracker.backend.transaction.TransactionStatus;
import com.financialtracker.backend.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final Clock clock;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository,
                              AccountRepository accountRepository,
                              Clock clock, TransactionRepository transactionRepository) {

        this.savingsGoalRepository = savingsGoalRepository;
        this.accountRepository = accountRepository;
        this.clock = clock;
        this.transactionRepository = transactionRepository;
    }

    public SavingsGoalResponse createSavingsGoal(SavingsGoalRequest request) {

        if (request.getCurrentAmount().compareTo(request.getTargetAmount()) > 0) {
            throw new InvalidSavingsGoalException(
                    "Current amount cannot be greater than target amount"
            );
        }
        SavingsGoal savingsGoal = new SavingsGoal();

        savingsGoal.setName(request.getName());
        savingsGoal.setTargetAmount(request.getTargetAmount());
        savingsGoal.setCurrentAmount(request.getCurrentAmount());
        savingsGoal.setTargetDate(request.getTargetDate());
        savingsGoal.setActive(true);

        SavingsGoal savedGoal = savingsGoalRepository.save(savingsGoal);

        return toSavingsGoalResponse(savedGoal);
    }

    public List<SavingsGoalResponse> getAllSavingsGoals() {
        return savingsGoalRepository.findAll()
                .stream()
                .map(this::toSavingsGoalResponse)
                .toList();
    }

    public SavingsGoalResponse getSavingsGoalById(Long id) {

        SavingsGoal savingsGoal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));

        return toSavingsGoalResponse(savingsGoal);
    }

    public SavingsGoalResponse updateSavingsGoal(Long id, SavingsGoalRequest request) {
        if (request.getCurrentAmount().compareTo(request.getTargetAmount()) > 0) {
            throw new InvalidSavingsGoalException(
                    "Current amount cannot be greater than target amount"
            );
        }
        SavingsGoal savingsGoal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));

        savingsGoal.setName(request.getName());
        savingsGoal.setTargetAmount(request.getTargetAmount());
        savingsGoal.setCurrentAmount(request.getCurrentAmount());
        savingsGoal.setTargetDate(request.getTargetDate());

        SavingsGoal savedGoal = savingsGoalRepository.save(savingsGoal);

        return toSavingsGoalResponse(savedGoal);
    }

    public SavingsGoalResponse deactivateSavingsGoal(Long id) {

        SavingsGoal savingsGoal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new SavingsGoalNotFoundException(id));

        savingsGoal.setActive(false);

        SavingsGoal savedGoal = savingsGoalRepository.save(savingsGoal);

        return toSavingsGoalResponse(savedGoal);
    }

    private SavingsGoalResponse toSavingsGoalResponse(
            SavingsGoal savingsGoal) {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(savingsGoal.getId());
        response.setName(savingsGoal.getName());
        response.setTargetAmount(savingsGoal.getTargetAmount());
        response.setCurrentAmount(savingsGoal.getCurrentAmount());

        BigDecimal remainingAmount =
                savingsGoal.getTargetAmount()
                        .subtract(savingsGoal.getCurrentAmount());

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        response.setRemainingAmount(remainingAmount);

        BigDecimal progressPercentage =
                savingsGoal.getCurrentAmount()
                        .divide(
                                savingsGoal.getTargetAmount(),
                                4,
                                java.math.RoundingMode.HALF_UP
                        )
                        .multiply(new BigDecimal("100"))
                        .setScale(2, java.math.RoundingMode.HALF_UP);

        if (progressPercentage.compareTo(new BigDecimal("100.00")) > 0) {
            progressPercentage = new BigDecimal("100.00");
        }

        response.setProgressPercentage(progressPercentage);
        YearMonth currentMonth = YearMonth.now(clock);
        YearMonth targetMonth = YearMonth.from(savingsGoal.getTargetDate());

        long monthsUntilTarget =
                java.time.temporal.ChronoUnit.MONTHS.between(
                        currentMonth,
                        targetMonth
                ) + 1;

        BigDecimal requiredMonthlySavings;

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            requiredMonthlySavings = BigDecimal.ZERO;
        } else if (monthsUntilTarget <= 0) {
            requiredMonthlySavings = remainingAmount;
        } else {
            requiredMonthlySavings =
                    remainingAmount
                            .divide(
                                    BigDecimal.valueOf(monthsUntilTarget),
                                    2,
                                    java.math.RoundingMode.CEILING
                            );
        }

        response.setRequiredMonthlySavings(requiredMonthlySavings);
        response.setTargetDate(savingsGoal.getTargetDate());
        response.setActive(savingsGoal.isActive());

        return response;
    }
    @Transactional
    public SavingsGoalResponse contributeToSavingsGoal(
            Long id,
            SavingsContributionRequest request) {

        SavingsGoal savingsGoal =
                savingsGoalRepository.findById(id)
                        .orElseThrow(() ->
                                new SavingsGoalNotFoundException(id));

        Account account =
                accountRepository.findById(request.getAccountId())
                        .orElseThrow(() ->
                                new AccountNotFoundException(
                                        request.getAccountId()
                                ));

        BigDecimal newCurrentAmount =
                savingsGoal.getCurrentAmount()
                        .add(request.getAmount());

        if (newCurrentAmount.compareTo(
                savingsGoal.getTargetAmount()) > 0) {

            throw new InvalidSavingsGoalException(
                    "Contribution would exceed the target amount"
            );
        }

        if (account.getCurrentBalance().compareTo(request.getAmount()) < 0) {

            throw new InsufficientAccountBalanceException(
                    "Insufficient account balance for this contribution"
            );
        }

        // Update savings goal
        savingsGoal.setCurrentAmount(newCurrentAmount);

        // Deduct contribution from account
        BigDecimal newAccountBalance =
                account.getCurrentBalance()
                        .subtract(request.getAmount());

        account.setCurrentBalance(newAccountBalance);

        // Record contribution as a transaction
        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setTransferAccount(null);
        transaction.setTransactionType(TransactionType.EXPENSE);
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setAmount(request.getAmount());
        transaction.setCategory("Savings");
        transaction.setDescription(
                "Contribution to " + savingsGoal.getName()
        );
        transaction.setTransactionDate(LocalDate.now(clock));
        transaction.setCreatedAt(LocalDateTime.now(clock));

        // Save changes
        accountRepository.save(account);
        transactionRepository.save(transaction);

        SavingsGoal savedGoal =
                savingsGoalRepository.save(savingsGoal);

        return toSavingsGoalResponse(savedGoal);
    }
}
