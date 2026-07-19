package org.mtech.ledger.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Account {

    private final UUID id;
    private BigDecimal balance = new BigDecimal("0.00");
    private final List<Transaction> history = new ArrayList<>();

    public Account(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public List<Transaction> getHistory() {
        return history;
    }

    /** Applies a transaction: sets the new balance and appends it to the history. */
    public void commit(Transaction transaction) {
        this.balance = transaction.balanceAfter();
        this.history.add(transaction);
    }
}
