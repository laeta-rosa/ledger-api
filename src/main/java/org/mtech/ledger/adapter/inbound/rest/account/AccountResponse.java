package org.mtech.ledger.adapter.inbound.rest.account;

import java.math.BigDecimal;
import java.util.UUID;
import org.mtech.ledger.application.account.AccountResult;

public record AccountResponse(UUID id, String name, String surname, BigDecimal balance) {

    public static AccountResponse from(AccountResult account) {
        return new AccountResponse(account.id(), account.name(), account.surname(), account.balance());
    }
}
