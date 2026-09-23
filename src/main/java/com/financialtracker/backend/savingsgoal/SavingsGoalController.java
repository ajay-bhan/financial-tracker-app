package com.financialtracker.backend.savingsgoal;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    public SavingsGoalController(SavingsGoalService savingsGoalService) {
        this.savingsGoalService = savingsGoalService;
    }

    @PostMapping
    public SavingsGoalResponse createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequest request) {

        return savingsGoalService.createSavingsGoal(request);
    }

    @GetMapping
    public List<SavingsGoalResponse> getAllSavingsGoals() {
        return savingsGoalService.getAllSavingsGoals();
    }

    @GetMapping("/{id}")
    public SavingsGoalResponse getSavingsGoalById(
            @PathVariable Long id) {

        return savingsGoalService.getSavingsGoalById(id);
    }

    @PutMapping("/{id}")
    public SavingsGoalResponse updateSavingsGoal(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalRequest request) {

        return savingsGoalService.updateSavingsGoal(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public SavingsGoalResponse deactivateSavingsGoal(
            @PathVariable Long id) {

        return savingsGoalService.deactivateSavingsGoal(id);
    }
}
