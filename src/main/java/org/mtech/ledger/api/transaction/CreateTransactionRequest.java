package org.mtech.ledger.api.transaction;

import org.mtech.ledger.api.transaction.validator.ValidAmount;

import java.math.BigDecimal;

public record CreateTransactionRequest(@ValidAmount BigDecimal amount) {
}
