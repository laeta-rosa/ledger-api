package org.mtech.ledger.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record Transaction(
        UUID id,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) {
}
