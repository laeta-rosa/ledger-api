package org.mtech.ledger.common.uuid;

import java.util.UUID;

/** Generates identifiers for new aggregates, keeping id creation injectable and testable. */
public interface UuidGenerator {

    UUID generate();
}
