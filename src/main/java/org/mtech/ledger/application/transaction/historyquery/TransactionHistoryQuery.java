package org.mtech.ledger.application.transaction.historyquery;

import java.util.UUID;

/** Request for an account's transaction history. */
public record TransactionHistoryQuery(UUID accountId) {
}
