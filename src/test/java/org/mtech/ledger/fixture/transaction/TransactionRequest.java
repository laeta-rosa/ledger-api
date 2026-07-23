package org.mtech.ledger.fixture.transaction;

import org.mtech.ledger.fixture.TestMessage;

/**
 * A deposit / withdrawal body. The amount is kept as the raw literal the test wrote,
 * so boundary cases ({@code 1.005}, {@code 1000000000000}) reach the API unmodified.
 */
public record TransactionRequest(String amount) implements TestMessage {

    @Override
    public String asJson() {
        return "{\"amount\":" + amount + "}";
    }
}