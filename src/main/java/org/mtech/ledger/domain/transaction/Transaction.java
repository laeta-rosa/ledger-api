package org.mtech.ledger.domain.transaction;

import java.time.Instant;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.vo.TransactionId;

/**
 * A single money movement in the append-only ledger, referencing the owning
 * account by id. Each transaction stores the resulting balance
 * ({@code balanceAfter}), so history doubles as an audit trail.
 */
public record Transaction(
        TransactionId id,
        AccountId accountId,
        TransactionType type,
        Money amount,
        Instant timestamp,
        Money balanceAfter) {
}
