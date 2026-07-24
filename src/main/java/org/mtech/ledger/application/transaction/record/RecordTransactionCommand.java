package org.mtech.ledger.application.transaction.record;

import java.math.BigDecimal;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.TransactionType;

/** Request to record a deposit or withdrawal against an account. */
public record RecordTransactionCommand(UUID accountId, TransactionType type, BigDecimal amount) {
}
