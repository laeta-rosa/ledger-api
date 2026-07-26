package org.mtech.ledger.domain.money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mtech.ledger.domain.vo.Money;

class MoneyTest {

    @ParameterizedTest
    @CsvSource({
            "5, 5.00",
            "5.5, 5.50",
            "5.50, 5.50",
            "5.500, 5.50",
            "0, 0.00",
    })
    void normalizesToScaleTwo(String raw, String expected) {
        assertThat(Money.of(raw).value()).isEqualTo(new BigDecimal(expected));
    }

    @Test
    void equalAmountsAreEqualValuesRegardlessOfInputScale() {
        assertThat(Money.of("5.5")).isEqualTo(Money.of("5.50"));
        assertThat(Money.of("5.500")).isEqualTo(Money.of("5.50"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.005", "0.001", "-0.01", "-10"})
    void rejectsTooManyDecimalsAndNegatives(String raw) {
        assertThatThrownBy(() -> Money.of(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> new Money(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void addsAndSubtracts() {
        assertThat(Money.of("10.00").add(Money.of("2.50"))).isEqualTo(Money.of("12.50"));
        assertThat(Money.of("10.00").subtract(Money.of("2.50"))).isEqualTo(Money.of("7.50"));
    }

    @Test
    void subtractingBelowZeroIsImpossible() {
        assertThatThrownBy(() -> Money.of("1.00").subtract(Money.of("2.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requirePositiveRejectsZero() {
        assertThat(Money.of("0.01").requirePositive()).isEqualTo(Money.of("0.01"));
        assertThatThrownBy(Money.ZERO::requirePositive).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void comparesByAmount() {
        assertThat(Money.of("2.00").isGreaterThan(Money.of("1.99"))).isTrue();
        assertThat(Money.of("2.00").isGreaterThan(Money.of("2.00"))).isFalse();
    }
}