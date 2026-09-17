package com.financialtracker.backend.budget;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void createBudget_shouldCreateBudgetSuccessfully() {

        BudgetRequest request = new BudgetRequest();
        request.setCategory("Food & Groceries");
        request.setMonthlyLimit(new BigDecimal("500.00"));
        request.setMonth(YearMonth.of(2026, 9));

        Budget savedBudget = new Budget();
        savedBudget.setId(1L);
        savedBudget.setCategory("Food & Groceries");
        savedBudget.setMonthlyLimit(new BigDecimal("500.00"));
        savedBudget.setMonth(YearMonth.of(2026, 9));
        savedBudget.setActive(true);

        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(savedBudget);

        BudgetResponse response =
                budgetService.createBudget(request);

        assertEquals(1L, response.getId());
        assertEquals("Food & Groceries", response.getCategory());
        assertEquals(
                new BigDecimal("500.00"),
                response.getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.getMonth()
        );
        assertTrue(response.isActive());

        verify(budgetRepository).save(any(Budget.class));
    }
    @Test
    void getAllBudgets_shouldReturnAllBudgets() {

        Budget budget1 = new Budget();
        budget1.setId(1L);
        budget1.setCategory("Food & Groceries");
        budget1.setMonthlyLimit(new BigDecimal("500.00"));
        budget1.setMonth(YearMonth.of(2026, 9));
        budget1.setActive(true);

        Budget budget2 = new Budget();
        budget2.setId(2L);
        budget2.setCategory("Rent");
        budget2.setMonthlyLimit(new BigDecimal("2000.00"));
        budget2.setMonth(YearMonth.of(2026, 9));
        budget2.setActive(true);

        when(budgetRepository.findAll())
                .thenReturn(java.util.List.of(budget1, budget2));

        var response = budgetService.getAllBudgets();

        assertEquals(2, response.size());

        assertEquals(1L, response.get(0).getId());
        assertEquals(
                "Food & Groceries",
                response.get(0).getCategory()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.get(0).getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.get(0).getMonth()
        );
        assertTrue(response.get(0).isActive());

        assertEquals(2L, response.get(1).getId());
        assertEquals("Rent", response.get(1).getCategory());
        assertEquals(
                new BigDecimal("2000.00"),
                response.get(1).getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.get(1).getMonth()
        );
        assertTrue(response.get(1).isActive());

        verify(budgetRepository).findAll();
    }
    @Test
    void getBudgetById_shouldReturnBudgetSuccessfully() {

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setCategory("Food & Groceries");
        budget.setMonthlyLimit(new BigDecimal("500.00"));
        budget.setMonth(YearMonth.of(2026, 9));
        budget.setActive(true);

        when(budgetRepository.findById(1L))
                .thenReturn(java.util.Optional.of(budget));

        BudgetResponse response =
                budgetService.getBudgetById(1L);

        assertEquals(1L, response.getId());
        assertEquals(
                "Food & Groceries",
                response.getCategory()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.getMonth()
        );
        assertTrue(response.isActive());

        verify(budgetRepository).findById(1L);
    }

    @Test
    void getBudgetById_shouldThrowExceptionWhenBudgetDoesNotExist() {

        when(budgetRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        BudgetNotFoundException exception =
                assertThrows(
                        BudgetNotFoundException.class,
                        () -> budgetService.getBudgetById(999L)
                );

        assertEquals(
                "Budget with id 999 not found",
                exception.getMessage()
        );

        verify(budgetRepository).findById(999L);
    }

    @Test
    void updateBudget_shouldUpdateBudgetSuccessfully() {

        Budget existingBudget = new Budget();
        existingBudget.setId(1L);
        existingBudget.setCategory("Food & Groceries");
        existingBudget.setMonthlyLimit(new BigDecimal("500.00"));
        existingBudget.setMonth(YearMonth.of(2026, 9));
        existingBudget.setActive(true);

        BudgetRequest request = new BudgetRequest();
        request.setCategory("Food & Groceries");
        request.setMonthlyLimit(new BigDecimal("600.00"));
        request.setMonth(YearMonth.of(2026, 9));

        Budget updatedBudget = new Budget();
        updatedBudget.setId(1L);
        updatedBudget.setCategory("Food & Groceries");
        updatedBudget.setMonthlyLimit(new BigDecimal("600.00"));
        updatedBudget.setMonth(YearMonth.of(2026, 9));
        updatedBudget.setActive(true);

        when(budgetRepository.findById(1L))
                .thenReturn(java.util.Optional.of(existingBudget));

        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(updatedBudget);

        BudgetResponse response =
                budgetService.updateBudget(1L, request);

        assertEquals(1L, response.getId());
        assertEquals(
                "Food & Groceries",
                response.getCategory()
        );
        assertEquals(
                new BigDecimal("600.00"),
                response.getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.getMonth()
        );
        assertTrue(response.isActive());

        verify(budgetRepository).findById(1L);
        verify(budgetRepository).save(existingBudget);
    }
    @Test
    void updateBudget_shouldThrowExceptionWhenBudgetDoesNotExist() {

        BudgetRequest request = new BudgetRequest();
        request.setCategory("Food & Groceries");
        request.setMonthlyLimit(new BigDecimal("600.00"));
        request.setMonth(YearMonth.of(2026, 9));

        when(budgetRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        BudgetNotFoundException exception =
                assertThrows(
                        BudgetNotFoundException.class,
                        () -> budgetService.updateBudget(999L, request)
                );

        assertEquals(
                "Budget with id 999 not found",
                exception.getMessage()
        );

        verify(budgetRepository).findById(999L);

        verify(budgetRepository, never())
                .save(any(Budget.class));
    }

    @Test
    void deactivateBudget_shouldDeactivateBudgetSuccessfully() {

        Budget budget = new Budget();
        budget.setId(1L);
        budget.setCategory("Food & Groceries");
        budget.setMonthlyLimit(new BigDecimal("500.00"));
        budget.setMonth(YearMonth.of(2026, 9));
        budget.setActive(true);

        Budget deactivatedBudget = new Budget();
        deactivatedBudget.setId(1L);
        deactivatedBudget.setCategory("Food & Groceries");
        deactivatedBudget.setMonthlyLimit(new BigDecimal("500.00"));
        deactivatedBudget.setMonth(YearMonth.of(2026, 9));
        deactivatedBudget.setActive(false);

        when(budgetRepository.findById(1L))
                .thenReturn(java.util.Optional.of(budget));

        when(budgetRepository.save(any(Budget.class)))
                .thenReturn(deactivatedBudget);

        BudgetResponse response =
                budgetService.deactivateBudget(1L);

        assertEquals(1L, response.getId());
        assertEquals(
                "Food & Groceries",
                response.getCategory()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.getMonthlyLimit()
        );
        assertEquals(
                YearMonth.of(2026, 9),
                response.getMonth()
        );
        assertFalse(response.isActive());

        verify(budgetRepository).findById(1L);
        verify(budgetRepository).save(budget);
    }
    @Test
    void deactivateBudget_shouldThrowExceptionWhenBudgetDoesNotExist() {

        when(budgetRepository.findById(999L))
                .thenReturn(java.util.Optional.empty());

        BudgetNotFoundException exception =
                assertThrows(
                        BudgetNotFoundException.class,
                        () -> budgetService.deactivateBudget(999L)
                );

        assertEquals(
                "Budget with id 999 not found",
                exception.getMessage()
        );

        verify(budgetRepository).findById(999L);

        verify(budgetRepository, never())
                .save(any(Budget.class));
    }
}
