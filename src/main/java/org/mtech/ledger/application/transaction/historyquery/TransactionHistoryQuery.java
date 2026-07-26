package org.mtech.ledger.application.transaction.historyquery;

import org.mtech.ledger.domain.vo.AccountId;

/** Request for an account's transaction history. */
public record TransactionHistoryQuery(AccountId accountId) {
}
