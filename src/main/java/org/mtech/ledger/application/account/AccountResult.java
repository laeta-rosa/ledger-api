package org.mtech.ledger.application.account;

import java.math.BigDecimal;
import java.util.UUID;
import org.mtech.ledger.domain.account.Account;

/** The result of an account command or query, decoupling adapters from the domain aggregate. */
public record AccountResult(UUID id, String name, String surname, BigDecimal balance) {

    public static AccountResult from(Account account) {
        return new AccountResult(
                account.getId(), account.getName(), account.getSurname(), account.getBalance());
    }
}
