package org.mtech.ledger.api.account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(UUID id, BigDecimal balance) {
}
