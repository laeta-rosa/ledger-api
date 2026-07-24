package org.mtech.ledger.adapter.inbound.rest.transaction.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.domain.transaction.TransactionType;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {

    public static TransactionResponse from(TransactionResult transaction) {
        return new TransactionResponse(
                transaction.id(),
                transaction.type(),
                transaction.amount(),
                transaction.timestamp(),
                transaction.balanceAfter());
    }
}
