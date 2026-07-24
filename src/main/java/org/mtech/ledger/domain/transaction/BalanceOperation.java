package org.mtech.ledger.domain.transaction;

import java.math.BigDecimal;
import java.util.function.BiConsumer;
import org.mtech.ledger.domain.account.Account;

/**
 * A balance-changing operation applied to an account, such as
 * {@link Account#deposit} or {@link Account#withdraw}. Naming the contract keeps
 * the intent explicit where a bare {@code BiConsumer<Account, BigDecimal>} would read as
 * "some function."
 */
@FunctionalInterface
public interface BalanceOperation extends BiConsumer<Account, BigDecimal> {
}