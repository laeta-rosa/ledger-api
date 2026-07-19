package org.mtech.ledger.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.Account;
import org.mtech.ledger.domain.AccountNotFoundException;
import org.mtech.ledger.domain.InsufficientFundsException;
import org.mtech.ledger.domain.Transaction;
import org.mtech.ledger.domain.TransactionType;
import org.mtech.ledger.repository.InMemoryAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class LedgerService {

    private final InMemoryAccountRepository repository;

    public LedgerService(InMemoryAccountRepository repository) {
        this.repository = repository;
    }

    public Account createAccount() {
        return repository.create();
    }

    public BigDecimal getBalance(UUID accountId) {
        return findAccount(accountId).getBalance();
    }

    public Transaction record(UUID accountId, TransactionType type, BigDecimal amount) {
        validateAmount(amount);
        Account account = findAccount(accountId);
        BigDecimal normalized = amount.setScale(2);

        // Balance update and history append must be consistent under concurrent requests.
        synchronized (account) {
            BigDecimal newBalance = switch (type) {
                case DEPOSIT -> account.getBalance().add(normalized);
                case WITHDRAWAL -> account.getBalance().subtract(normalized);
            };
            if (newBalance.signum() < 0) {
                throw new InsufficientFundsException(account.getBalance(), normalized);
            }
            Transaction transaction =
                    new Transaction(UUID.randomUUID(), type, normalized, Instant.now(), newBalance);
            account.commit(transaction);
            return transaction;
        }
    }

    /** Returns the account's transactions, newest first. */
    public List<Transaction> getHistory(UUID accountId) {
        Account account = findAccount(accountId);
        synchronized (account) {
            return List.copyOf(account.getHistory()).reversed();
        }
    }

    private Account findAccount(UUID accountId) {
        return repository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
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
