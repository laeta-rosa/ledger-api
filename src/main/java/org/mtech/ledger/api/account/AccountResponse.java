package org.mtech.ledger.api.account;

import java.math.BigDecimal;
import java.util.UUID;
import org.mtech.ledger.domain.account.Account;

public record AccountResponse(UUID id, String name, String surname, BigDecimal balance) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(), account.getName(), account.getSurname(), account.getBalance());
    }
}
