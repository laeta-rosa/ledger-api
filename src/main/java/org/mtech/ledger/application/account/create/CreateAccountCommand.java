package org.mtech.ledger.application.account.create;

/** Request to open a new account for the given holder. */
public record CreateAccountCommand(String name, String surname) {
}
