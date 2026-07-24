package org.mtech.ledger.integration.fixture.account;

import org.mtech.ledger.integration.fixture.TestMessage;

/**
 * A {@code POST /accounts} body. Named constants stand in for the recurring account
 * holders so tests and their assertions draw the same name from one place.
 */
public record AccountRequest(String name, String surname) implements TestMessage {

    public static final AccountRequest ADA = new AccountRequest("Ada", "Lovelace");

    public static final AccountRequest GRACE = new AccountRequest("Grace", "Hopper");

    public static AccountRequest withName(String name) {
        return new AccountRequest(name, GRACE.surname());
    }

    @Override
    public String asJson() {
        return "{\"name\":\"%s\",\"surname\":\"%s\"}".formatted(name, surname);
    }
}
