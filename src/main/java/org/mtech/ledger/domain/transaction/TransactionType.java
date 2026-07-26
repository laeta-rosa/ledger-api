package org.mtech.ledger.domain.transaction;

import lombok.AllArgsConstructor;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.Money;

@AllArgsConstructor
public enum TransactionType {
    DEPOSIT(Account::deposit),
    WITHDRAWAL(Account::withdraw);

    private final BalanceOperation action;

    public void apply(Account account, Money amount) {
        this.action.accept(account, amount);
    }
}
