package org.mtech.ledger.domain.account;

import org.mtech.ledger.domain.vo.AccountId;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(AccountId accountId) {
        super("Account not found: " + accountId);
    }
}