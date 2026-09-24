package com.financialtracker.backend.savingsgoal;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final Clock clock;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository, Clock clock) {
        this.savingsGoalRepository = savingsGoalRepository;
        this.clock = clock;
    }

    public SavingsGoalResponse createSavingsGoal(SavingsGoalRequest request) {

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

    public SavingsGoalResponse updateSavingsGoal(
            Long id,
            SavingsGoalRequest request) {

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
}
