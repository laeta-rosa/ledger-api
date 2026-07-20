package org.mtech.ledger.domain.transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A single money movement, stored in its own table and referencing the owning
 * account by id. The ledger is append-only — transactions are never updated —
 * so {@link #isNew()} always returns {@code true}, telling Spring Data JDBC to
 * INSERT even though the id is assigned by us rather than the database.
 */
@Table("account_transaction")
public record Transaction(
        @Id UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) implements Persistable<UUID> {

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
