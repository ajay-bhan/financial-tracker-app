package com.financialtracker.backend.budget;

import org.springframework.stereotype.Service;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public BudgetResponse createBudget(BudgetRequest request) {

        if (budgetRepository.existsByCategoryIgnoreCaseAndMonth(
                request.getCategory(),
                request.getMonth())) {

            throw new BudgetAlreadyExistsException(
                    request.getCategory(),
                    request.getMonth()
            );
        }

        Budget budget = new Budget();

        budget.setCategory(request.getCategory());
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth());
        budget.setActive(true);

        Budget savedBudget = budgetRepository.save(budget);

        return toBudgetResponse(savedBudget);
    }

    public java.util.List<BudgetResponse> getAllBudgets() {

        return budgetRepository.findAll()
                .stream()
                .map(this::toBudgetResponse)
                .toList();
    }

    public BudgetResponse getBudgetById(Long id) {

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));

        return toBudgetResponse(budget);
    }
    public BudgetResponse updateBudget(Long id, BudgetRequest request) {

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));

        if (budgetRepository.existsByCategoryIgnoreCaseAndMonthAndIdNot(
                request.getCategory(),
                request.getMonth(),
                id)) {

            throw new BudgetAlreadyExistsException(
                    request.getCategory(),
                    request.getMonth()
            );
        }

        budget.setCategory(request.getCategory());
        budget.setMonthlyLimit(request.getMonthlyLimit());
        budget.setMonth(request.getMonth());

        Budget savedBudget = budgetRepository.save(budget);

        return toBudgetResponse(savedBudget);
    }

    public BudgetResponse deactivateBudget(Long id) {

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetNotFoundException(id));

        budget.setActive(false);

        Budget savedBudget = budgetRepository.save(budget);

        return toBudgetResponse(savedBudget);
    }

    private BudgetResponse toBudgetResponse(Budget budget) {

        BudgetResponse response = new BudgetResponse();

        response.setId(budget.getId());
        response.setCategory(budget.getCategory());
        response.setMonthlyLimit(budget.getMonthlyLimit());
        response.setMonth(budget.getMonth());
        response.setActive(budget.isActive());

        return response;
    }
}
