package org.mtech.ledger.integration.harness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Fixed {@link Clock} replacing the production one in integration tests, so
 * transaction timestamps are deterministic and assertable.
 */
@Configuration(proxyBeanMethods = false)
public class FixedClockConfiguration {

    /** The instant every integration-test transaction is stamped with. */
    public static final Instant FIXED_INSTANT = Instant.parse("2026-07-27T12:00:00Z");

    @Bean
    @Primary
    public Clock fixedClock() {
        return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
    }
}
