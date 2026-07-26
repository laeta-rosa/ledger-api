package org.mtech.ledger.application.account.balancequery;

import org.mtech.ledger.domain.vo.AccountId;

/** Request for the current state (including balance) of an account. */
public record AccountBalanceQuery(AccountId accountId) {
}
