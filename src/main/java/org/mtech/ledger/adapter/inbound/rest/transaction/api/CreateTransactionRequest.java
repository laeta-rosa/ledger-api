package org.mtech.ledger.adapter.inbound.rest.transaction.api;

import org.mtech.ledger.adapter.inbound.rest.transaction.api.validator.ValidAmount;

import java.math.BigDecimal;

public record CreateTransactionRequest(@ValidAmount BigDecimal amount) {
}
