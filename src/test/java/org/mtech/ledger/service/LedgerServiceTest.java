package org.mtech.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.Account;
import org.mtech.ledger.domain.AccountNotFoundException;
import org.mtech.ledger.domain.InsufficientFundsException;
import org.mtech.ledger.domain.Transaction;
import org.mtech.ledger.domain.TransactionType;
import org.mtech.ledger.repository.InMemoryAccountRepository;

class LedgerServiceTest {

    private LedgerService service;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        service = new LedgerService(new InMemoryAccountRepository());
        Account account = service.createAccount();
        accountId = account.getId();
    }

    @Test
    void newAccountHasZeroBalance() {
        assertThat(service.getBalance(accountId)).isEqualByComparingTo("0.00");
    }

    @Test
    void depositIncreasesBalance() {
        Transaction tx = service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.50"));

        assertThat(tx.balanceAfter()).isEqualByComparingTo("100.50");
        assertThat(service.getBalance(accountId)).isEqualByComparingTo("100.50");
    }

    @Test
    void withdrawalDecreasesBalance() {
        service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.00"));
        service.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("30.00"));

        assertThat(service.getBalance(accountId)).isEqualByComparingTo("70.00");
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("10.00"));

        assertThatThrownBy(() ->
                service.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("10.01")))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(service.getBalance(accountId)).isEqualByComparingTo("10.00");
    }

    @Test
    void zeroOrNegativeAmountIsRejected() {
        assertThatThrownBy(() ->
                service.record(accountId, TransactionType.DEPOSIT, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("-5.00")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void amountWithMoreThanTwoDecimalPlacesIsRejected() {
        assertThatThrownBy(() ->
                service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("1.005")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unknownAccountIsRejected() {
        UUID unknown = UUID.randomUUID();

        assertThatThrownBy(() -> service.getBalance(unknown))
                .isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() ->
                service.record(unknown, TransactionType.DEPOSIT, new BigDecimal("1.00")))
                .isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> service.getHistory(unknown))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void historyIsNewestFirstAndTracksBalance() {
        service.record(accountId, TransactionType.DEPOSIT, new BigDecimal("100.00"));
        service.record(accountId, TransactionType.WITHDRAWAL, new BigDecimal("40.00"));

        List<Transaction> history = service.getHistory(accountId);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(history.get(0).balanceAfter()).isEqualByComparingTo("60.00");
        assertThat(history.get(1).type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(1).balanceAfter()).isEqualByComparingTo("100.00");
    }
}
