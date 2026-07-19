package org.mtech.ledger.domain;

import java.math.BigDecimal;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(BigDecimal balance, BigDecimal requested) {
        super("Insufficient funds: balance is " + balance + ", requested withdrawal of " + requested);
    }
}
