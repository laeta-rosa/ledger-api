package org.mtech.ledger.integration.harness;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.mtech.ledger.common.uuid.UuidGenerator;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Deterministic {@link UuidGenerator} replacing the production one in integration tests.
 * Yields the sequence {@code 00000000-0000-0000-0000-000000000001}, {@code ...002}, ... —
 * a sequence rather than a constant because tests insert several rows per test and ids are primary keys.
 * Tests asserting exact ids should call {@link #reset()} in their {@code @BeforeEach}.
 */
@Component
@Primary
public class FixedUuidGenerator implements UuidGenerator {

    private final AtomicLong counter = new AtomicLong();

    @Override
    public UUID generate() {
        return fixedUuid(counter.incrementAndGet());
    }

    public void reset() {
        counter.set(0);
    }

    /** The n-th UUID of the sequence, for assertions: {@code fixedUuid(1)} is the first generated id. */
    public static UUID fixedUuid(long n) {
        return new UUID(0, n);
    }
}
