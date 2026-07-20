package org.mtech.ledger.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.domain.Transaction;
import org.mtech.ledger.domain.TransactionType;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.type(),
                transaction.amount(),
                transaction.timestamp(),
                transaction.balanceAfter());
    }
}
