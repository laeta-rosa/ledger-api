package org.mtech.ledger.domain.transaction;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import org.mtech.ledger.domain.account.Account;

@AllArgsConstructor
public enum TransactionType {
    DEPOSIT(Account::deposit),
    WITHDRAWAL(Account::withdraw);

    private final BalanceOperation action;

    public void apply(Account account, BigDecimal amount) {
        this.action.accept(account, amount);
    }
}