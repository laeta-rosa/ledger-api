package org.mtech.ledger.fixture;

/** A request body a test can hand to {@code RestTestHarness} as its own JSON. */
public interface TestMessage {

    String asJson();
}