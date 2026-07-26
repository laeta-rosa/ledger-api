package org.mtech.ledger.domain.vo;

import org.mtech.ledger.domain.account.Account;

import java.util.UUID;

import static java.util.Objects.requireNonNull;

/** Typed identifier of an {@link Account}. */
public record AccountId(UUID value) {

    public AccountId {
        requireNonNull(value, "account id must not be null");
    }

    public static AccountId of(UUID value) {
        return new AccountId(value);
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
