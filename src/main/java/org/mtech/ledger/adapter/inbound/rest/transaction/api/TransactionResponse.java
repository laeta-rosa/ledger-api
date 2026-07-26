package org.mtech.ledger.adapter.inbound.rest.transaction.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.application.transaction.TransactionResult.Success.FoundTransaction;
import org.mtech.ledger.domain.transaction.TransactionType;

public record TransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {

    public static TransactionResponse from(FoundTransaction transaction) {
        return new TransactionResponse(
                transaction.id().value(),
                transaction.type(),
                transaction.amount().value(),
                transaction.timestamp(),
                transaction.balanceAfter().value());
    }
}
