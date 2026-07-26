package org.mtech.ledger.application.account;

import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;

/**
 * The result of an account command or query, decoupling adapters from the domain aggregate.
 */
public sealed interface AccountResult {

    record Success(AccountId id, String name, String surname, Money balance) implements AccountResult {

        public static Success of(Account account) {
            return new Success(account.getId(), account.getName(), account.getSurname(), account.getBalance());
        }
    }

    record NotFound(AccountId id) implements AccountResult {
    }
}
