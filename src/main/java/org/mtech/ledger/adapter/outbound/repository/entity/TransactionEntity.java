package org.mtech.ledger.adapter.outbound.repository.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.vo.TransactionId;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Database representation of a {@link Transaction}.
 */
@Table("account_transaction")
public record TransactionEntity(
        @Id UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        Instant timestamp,
        BigDecimal balanceAfter) implements Persistable<UUID> {

    public static TransactionEntity from(Transaction transaction) {
        return new TransactionEntity(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.type(),
                transaction.amount().value(),
                transaction.timestamp(),
                transaction.balanceAfter().value());
    }

    public Transaction toDomain() {
        return new Transaction(
                TransactionId.of(id),
                AccountId.of(accountId),
                type,
                Money.of(amount),
                timestamp,
                Money.of(balanceAfter));
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
