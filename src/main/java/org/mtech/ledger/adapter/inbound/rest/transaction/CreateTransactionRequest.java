package org.mtech.ledger.adapter.inbound.rest.transaction;

import org.mtech.ledger.adapter.inbound.rest.transaction.validator.ValidAmount;

import java.math.BigDecimal;

public record CreateTransactionRequest(@ValidAmount BigDecimal amount) {
}
