package org.mtech.ledger.application.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.transaction.TransactionType;

/** The result of a transaction command or query, decoupling adapters from the domain record. */
public record TransactionResult(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {

    public static TransactionResult from(Transaction transaction) {
        return new TransactionResult(
                transaction.id(),
                transaction.type(),
                transaction.amount(),
                transaction.timestamp(),
                transaction.balanceAfter());
    }
}
