package org.mtech.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.account.InsufficientFundsException;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TransactionServiceTest {

    @Autowired
    private AccountService accounts;

    @Autowired
    private TransactionService transactions;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = accounts.createAccount().getId();
    }

    @Test
    void depositIncreasesBalance() {
        var tx = transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.50"));

        assertThat(tx.balanceAfter()).isEqualByComparingTo("100.50");
        assertThat(accounts.getBalance(accountId)).isEqualByComparingTo("100.50");
    }

    @Test
    void withdrawalDecreasesBalance() {
        transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.00"));
        transactions.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("30.00"));

        assertThat(accounts.getBalance(accountId)).isEqualByComparingTo("70.00");
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("10.00"));

        assertThatThrownBy(() ->
                transactions.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("10.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(accounts.getBalance(accountId)).isEqualByComparingTo("10.00");
    }

    @Test
    void zeroOrNegativeAmountIsRejected() {
        assertThatThrownBy(() ->
                transactions.record(accountId, TransactionType.DEPOSIT, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("-5.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void amountWithMoreThanTwoDecimalPlacesIsRejected() {
        assertThatThrownBy(() ->
                transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("1.005")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownAccountIsRejected() {
        var unknown = UUID.randomUUID();

        assertThatThrownBy(() ->
                transactions.record(unknown, TransactionType.DEPOSIT, new BigDecimal("1.00")))
                .isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> transactions.getHistory(unknown))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void historyIsNewestFirstAndTracksBalance() {
        transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.00"));
        transactions.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("40.00"));

        var history = transactions.getHistory(accountId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(history.get(0).balanceAfter()).isEqualByComparingTo("60.00");
        assertThat(history.get(1).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(1).balanceAfter()).isEqualByComparingTo("100.00");
    }

    @Test
    void historyIsScopedToOneAccount() {
        var other = accounts.createAccount().getId();
        transactions.record(accountId, TransactionType.DEPOSIT, new BigDecimal("5.00"));

        assertThat(transactions.getHistory(other)).isEmpty();
        assertThat(transactions.getHistory(accountId)).hasSize(1);
    }
}