package org.mtech.ledger.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.util.Objects.requireNonNull;

/**
 * A non-negative monetary amount, always normalized to scale 2 so that equal
 * amounts are equal values ({@code 5.5} and {@code 5.500} both become
 * {@code 5.50}). Construction rejects nulls, negative values, and amounts with
 * more than 2 significant decimal places, so no unnormalized amount can exist
 * anywhere in the domain. Zero is allowed — a balance may be 0.00.
 */
public record Money(BigDecimal value) implements Comparable<Money> {

    public static final Money ZERO = of(BigDecimal.ZERO);

    public Money {
        requireNonNull(value, "amount must not be null");
        if (value.signum() < 0) {
            throw new IllegalArgumentException("amount must not be negative: " + value.toPlainString());
        }
        if (value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "amount must have at most 2 decimal places: " + value.toPlainString());
        }
        value = value.setScale(2, RoundingMode.UNNECESSARY);
    }

    public static Money of(BigDecimal value) {
        return new Money(value);
    }

    public static Money of(String value) {
        return of(new BigDecimal(value));
    }

    public Money add(Money other) {
        return of(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return of(this.value.subtract(other.value));
    }

    public Money requirePositive() {
        if (this.value.signum() == 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return this;
    }

    public boolean isGreaterThan(Money other) {
        return this.value.compareTo(other.value) > 0;
    }

    @Override
    public int compareTo(Money other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return this.value.toPlainString();
    }
}