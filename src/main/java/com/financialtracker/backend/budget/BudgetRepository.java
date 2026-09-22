package com.financialtracker.backend.budget;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    boolean existsByCategoryIgnoreCaseAndMonth(
            String category,
            YearMonth month
    );

    boolean existsByCategoryIgnoreCaseAndMonthAndIdNot(
                String category,
                YearMonth month,
                Long id
        );
    }

