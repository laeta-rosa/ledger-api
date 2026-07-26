package org.mtech.ledger.api.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.transaction.TransactionType;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id().value(),
                transaction.type(),
                transaction.amount().value(),
                transaction.timestamp(),
                transaction.balanceAfter().value());
    }
}