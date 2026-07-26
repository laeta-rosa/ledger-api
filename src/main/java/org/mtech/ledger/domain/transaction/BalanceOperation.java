package org.mtech.ledger.domain.transaction;

import java.util.function.BiConsumer;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.Money;

/**
 * A balance-changing operation applied to an account, such as
 * {@link Account#deposit} or {@link Account#withdraw}.
 */
@FunctionalInterface
public interface BalanceOperation extends BiConsumer<Account, Money> {
}
