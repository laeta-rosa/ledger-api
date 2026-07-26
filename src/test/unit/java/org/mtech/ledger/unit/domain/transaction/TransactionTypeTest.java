package org.mtech.ledger.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.account.InsufficientFundsException;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;

class TransactionTypeTest {

    @Test
    void depositAddsToBalance() {
        var account = Account.open(AccountId.of(UUID.randomUUID()), "Ada", "Lovelace");

        TransactionType.DEPOSIT.apply(account, Money.of("40.00"));

        assertThat(account.getBalance()).isEqualTo(Money.of("40.00"));
    }

    @Test
    void withdrawalSubtractsFromBalance() {
        var account = Account.open(AccountId.of(UUID.randomUUID()), "Ada", "Lovelace");
        TransactionType.DEPOSIT.apply(account, Money.of("40.00"));

        TransactionType.WITHDRAWAL.apply(account, Money.of("15.00"));

        assertThat(account.getBalance()).isEqualTo(Money.of("25.00"));
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        var account = Account.open(AccountId.of(UUID.randomUUID()), "Ada", "Lovelace");
        TransactionType.DEPOSIT.apply(account, Money.of("10.00"));

        assertThatThrownBy(() -> TransactionType.WITHDRAWAL.apply(account, Money.of("15.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }
}
