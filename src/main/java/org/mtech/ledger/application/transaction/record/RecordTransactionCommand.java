package org.mtech.ledger.application.transaction.record;

import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;

/** Request to record a deposit or withdrawal against an account. */
public record RecordTransactionCommand(AccountId accountId, TransactionType type, Money amount) {
}
