package org.mtech.ledger.unit.domain.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.account.InsufficientFundsException;
import org.mtech.ledger.domain.transaction.TransactionType;

class TransactionTypeTest {

    @Test
    void depositAddsToBalance() {
        var account = Account.open("Ada", "Lovelace");

        TransactionType.DEPOSIT.apply(account, new BigDecimal("40.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("40.00");
    }

    @Test
    void withdrawalSubtractsFromBalance() {
        var account = Account.open("Ada", "Lovelace");
        TransactionType.DEPOSIT.apply(account, new BigDecimal("40.00"));

        TransactionType.WITHDRAWAL.apply(account, new BigDecimal("15.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("25.00");
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        var account = Account.open("Ada", "Lovelace");
        TransactionType.DEPOSIT.apply(account, new BigDecimal("10.00"));

        assertThatThrownBy(() -> TransactionType.WITHDRAWAL.apply(account, new BigDecimal("15.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }
}
