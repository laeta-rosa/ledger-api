package org.mtech.ledger.api.transaction.validator;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mtech.ledger.api.transaction.CreateTransactionRequest;

class AmountValidatorTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "0.01", // smallest positive amount
                "1.00", // a clean two-decimal value
                "5.500", // trailing zero stripped to 5.50 — still two places
                "999999999999.99" // 12 integer digits: the accepted boundary
            })
    void acceptsValidAmounts(String amount) {
        assertThat(violations(new BigDecimal(amount))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "0", // not positive
                "-5.00", // negative
                "1.005", // three decimal places
                "1000000000000", // 13 integer digits: one past the limit
                "1000000000000.00" // 13 integer digits carrying a scale
            })
    void rejectsInvalidAmounts(String amount) {
        assertThat(violations(new BigDecimal(amount))).isNotEmpty();
    }

    @Test
    void rejectsNullAmount() {
        assertThat(violations(null)).isNotEmpty();
    }

    private Set<ConstraintViolation<CreateTransactionRequest>> violations(BigDecimal amount) {
        return validator.validate(new CreateTransactionRequest(amount));
    }
}