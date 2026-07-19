package org.mtech.ledger.api;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import org.mtech.ledger.domain.TransactionType;

public record CreateTransactionRequest(
        @NotNull TransactionType type,
        @NotNull @Positive @Digits(integer = 12, fraction = 2) BigDecimal amount) {
}
