package com.financialtracker.backend.budget;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public BudgetResponse createBudget(
            @Valid @RequestBody BudgetRequest request) {

        return budgetService.createBudget(request);
    }

    @GetMapping
    public List<BudgetResponse> getAllBudgets() {

        return budgetService.getAllBudgets();
    }

    @GetMapping("/{id}")
    public BudgetResponse getBudgetById(
            @PathVariable Long id) {

        return budgetService.getBudgetById(id);
    }

    @PutMapping("/{id}")
    public BudgetResponse updateBudget(
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        return budgetService.updateBudget(id, request);
    }

    @PatchMapping("/{id}/deactivate")
    public BudgetResponse deactivateBudget(
            @PathVariable Long id) {

        return budgetService.deactivateBudget(id);
    }
}
