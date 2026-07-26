package org.mtech.ledger.domain.vo;

import org.mtech.ledger.domain.transaction.Transaction;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

/** Typed identifier of a {@link Transaction}. */
public record TransactionId(UUID value) {

    public TransactionId {
        requireNonNull(value, "transaction id must not be null");
    }

    public static TransactionId of(UUID value) {
        return new TransactionId(value);
    }

    public static TransactionId random() {
        return of(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}