package org.mtech.ledger.application.account.balancequery;

import java.util.UUID;

/** Request for the current state (including balance) of an account. */
public record AccountBalanceQuery(UUID accountId) {
}
