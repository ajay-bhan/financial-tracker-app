package com.financialtracker.backend.transaction;

import com.financialtracker.backend.account.Account;
import com.financialtracker.backend.account.AccountNotFoundException;
import com.financialtracker.backend.account.AccountRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {

        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() ->
                        new AccountNotFoundException(request.getAccountId()));

        Account transferAccount = null;

        if (request.getTransactionType() == TransactionType.TRANSFER) {

            if (request.getTransferAccountId() == null) {
                throw new IllegalArgumentException(
                        "transferAccountId is required for transfers"
                );
            }

            transferAccount = accountRepository
                    .findById(request.getTransferAccountId())
                    .orElseThrow(() ->
                            new AccountNotFoundException(
                                    request.getTransferAccountId()
                            ));

            if (account.getId().equals(transferAccount.getId())) {
                throw new IllegalArgumentException(
                        "Source and destination accounts must be different"
                );
            }
        }

        Transaction transaction = new Transaction();

        transaction.setAccount(account);
        transaction.setTransferAccount(transferAccount);
        transaction.setTransactionType(request.getTransactionType());
        transaction.setStatus(TransactionStatus.ACTIVE);
        transaction.setAmount(request.getAmount());
        transaction.setCategory(request.getCategory());
        transaction.setDescription(request.getDescription());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setCreatedAt(LocalDateTime.now());

        if (request.getTransactionType() == TransactionType.INCOME) {

            account.setCurrentBalance(
                    account.getCurrentBalance().add(request.getAmount())
            );

        } else if (request.getTransactionType() == TransactionType.EXPENSE) {

            account.setCurrentBalance(
                    account.getCurrentBalance().subtract(request.getAmount())
            );

        } else if (request.getTransactionType() == TransactionType.TRANSFER) {

            account.setCurrentBalance(
                    account.getCurrentBalance().subtract(request.getAmount())
            );

            transferAccount.setCurrentBalance(
                    transferAccount.getCurrentBalance().add(request.getAmount())
            );

            accountRepository.save(transferAccount);
        }

        accountRepository.save(account);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return toTransactionResponse(savedTransaction);
    }

    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    public TransactionResponse getTransactionById(Long id) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                new TransactionNotFoundException(id));

        return toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse reverseTransaction(Long id) {

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() ->
                        new TransactionNotFoundException(id));

        if (transaction.getStatus() == TransactionStatus.REVERSED) {
            throw new IllegalArgumentException(
                    "Transaction with id " + id + " has already been reversed"
            );
        }

        Account account = transaction.getAccount();

        if (transaction.getTransactionType() == TransactionType.INCOME) {

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            .subtract(transaction.getAmount())
            );

        } else if (transaction.getTransactionType() == TransactionType.EXPENSE) {

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            .add(transaction.getAmount())
            );

        } else if (transaction.getTransactionType() == TransactionType.TRANSFER) {

            Account transferAccount = transaction.getTransferAccount();

            account.setCurrentBalance(
                    account.getCurrentBalance()
                            .add(transaction.getAmount())
            );

            transferAccount.setCurrentBalance(
                    transferAccount.getCurrentBalance()
                            .subtract(transaction.getAmount())
            );

            accountRepository.save(transferAccount);
        }

        transaction.setStatus(TransactionStatus.REVERSED);

        accountRepository.save(account);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return toTransactionResponse(savedTransaction);
    }

    private TransactionResponse toTransactionResponse(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setId(transaction.getId());
        if (transaction.getTransferAccount() != null) {
            response.setTransferAccountId(
                    transaction.getTransferAccount().getId()
            );
        }
        response.setAccountId(transaction.getAccount().getId());
        response.setTransactionType(transaction.getTransactionType());
        response.setStatus(transaction.getStatus());
        response.setAmount(transaction.getAmount());
        response.setCategory(transaction.getCategory());
        response.setDescription(transaction.getDescription());
        response.setTransactionDate(transaction.getTransactionDate());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }
}
