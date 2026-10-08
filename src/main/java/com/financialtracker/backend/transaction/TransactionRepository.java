package com.financialtracker.backend.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByTransactionTypeAndStatus(
            TransactionType transactionType,
            TransactionStatus status
    );

    List<Transaction> findByTransactionTypeAndStatusAndCategoryAndTransactionDateBetween(
            TransactionType transactionType,
            TransactionStatus status,
            String category,
            LocalDate startDate,
            LocalDate endDate
    );
}
