package org.mtech.ledger.adapter.inbound.rest.transaction.api.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validates a monetary amount: present, strictly positive, at most 12 integer
 * digits, and no more than 2 significant decimal places. Trailing zeros do not
 * count against the decimal limit, so {@code 5.500} is accepted (it is exactly
 * {@code 5.50}).
 */
@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
public @interface ValidAmount {

    String message() default "must be a positive amount with at most 2 decimal places";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
