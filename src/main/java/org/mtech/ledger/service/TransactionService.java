package org.mtech.ledger.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Money movements: recording deposits/withdrawals and reading history. */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountService accounts;
    private final TransactionRepository transactions;

    /**
     * Records a deposit or withdrawal atomically: the balance update and the
     * appended transaction commit together. The account's optimistic-lock
     * version guards against lost updates from concurrent movements, so an
     * overdraft can never slip through a race.
     */
    @Transactional
    public Transaction record(UUID accountId, TransactionType transactionType, BigDecimal amount) {
        validateAmount(amount);
        var account = accounts.getAccount(accountId);
        var normalized = amount.setScale(2);

        transactionType.apply(account, normalized);

        accounts.save(account);

        var transaction = new Transaction(
                UUID.randomUUID(), accountId, transactionType, normalized, Instant.now(), account.getBalance());
        return transactions.save(transaction);
    }

    /** Returns the account's transactions, newest first. */
    @Transactional(readOnly = true)
    public List<Transaction> getHistory(UUID accountId) {
        accounts.getAccount(accountId);
        return transactions.findByAccountIdOrderByTimestampDesc(accountId);
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be a positive number");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Amount cannot have more than 2 decimal places");
        }
    }
}