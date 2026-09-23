package com.financialtracker.backend.savingsgoal;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    public SavingsGoalService(SavingsGoalRepository savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
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
        response.setTargetDate(savingsGoal.getTargetDate());
        response.setActive(savingsGoal.isActive());

        return response;
    }
}
