package org.mtech.ledger.domain.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;

/**
 * Aggregate root for a ledger account. The current balance is the single
 * invariant this aggregate protects, so the balance-changing rules live here.
 */
@Getter
@AllArgsConstructor
public class Account {

    private final AccountId id;

    private final String name;

    private final String surname;

    private Money balance;

    private final Long version;

    public static Account open(String name, String surname) {
        return new Account(AccountId.random(), name.strip(), surname.strip(), Money.ZERO, null);
    }

    public void deposit(Money amount) {
        this.balance = this.balance.add(amount.requirePositive());
    }

    public void withdraw(Money amount) {
        if (amount.requirePositive().isGreaterThan(this.balance)) {
            throw new InsufficientFundsException(this.balance, amount);
        }
        this.balance = this.balance.subtract(amount);
    }
}