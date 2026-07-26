package org.mtech.ledger.domain.account;

import org.mtech.ledger.domain.vo.Money;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Money balance, Money requested) {
        super("Insufficient funds: balance is " + balance + ", requested withdrawal of " + requested);
    }
}
