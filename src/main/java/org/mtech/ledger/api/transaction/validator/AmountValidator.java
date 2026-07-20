package org.mtech.ledger.api.transaction.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

/**
 * Enforces {@link ValidAmount}. Decimal places are counted after stripping
 * trailing zeros, so {@code 5.500} passes while {@code 1.005} does not.
 */
public class AmountValidator implements ConstraintValidator<ValidAmount, BigDecimal> {

    private static final int MAX_INTEGER_DIGITS = 12;
    private static final int MAX_FRACTION_DIGITS = 2;

    @Override
    public boolean isValid(BigDecimal amount, ConstraintValidatorContext context) {
        var message = violation(amount);
        if (message == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
        return false;
    }

    private String violation(BigDecimal amount) {
        if (amount == null) {
            return "must not be null";
        }
        if (amount.signum() <= 0) {
            return "must be a positive number";
        }
        var stripped = amount.stripTrailingZeros();
        if (stripped.scale() > MAX_FRACTION_DIGITS) {
            return "must have at most 2 decimal places";
        }
        // precision - scale is the number of digits left of the decimal point.
        if (stripped.precision() - stripped.scale() > MAX_INTEGER_DIGITS) {
            return "must have at most 12 integer digits";
        }
        return null;
    }
}