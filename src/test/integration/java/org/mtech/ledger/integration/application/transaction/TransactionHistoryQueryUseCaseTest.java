package org.mtech.ledger.integration.application.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mtech.ledger.integration.harness.FixedUuidGenerator.fixedUuid;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.create.CreateAccountCommand;
import org.mtech.ledger.application.account.create.CreateAccountUseCase;
import org.mtech.ledger.application.transaction.historyquery.TransactionHistoryQuery;
import org.mtech.ledger.application.transaction.historyquery.TransactionHistoryQueryUseCase;
import org.mtech.ledger.application.transaction.record.RecordTransactionCommand;
import org.mtech.ledger.application.transaction.record.RecordTransactionUseCase;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.integration.harness.FixedUuidGenerator;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class TransactionHistoryQueryUseCaseTest {

    private final CreateAccountUseCase createAccount;
    private final RecordTransactionUseCase recordTransaction;
    private final TransactionHistoryQueryUseCase getTransactionHistory;
    private final FixedUuidGenerator uuids;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        uuids.reset();
        accountId = createAccount.invoke(new CreateAccountCommand("Ada", "Lovelace")).id();
    }

    @Test
    void historyIsNewestFirstAndTracksBalance() {
        record(TransactionType.DEPOSIT, "100.00");
        record(TransactionType.WITHDRAWAL, "40.00");

        var history = getTransactionHistory.invoke(new TransactionHistoryQuery(accountId));

        assertThat(history).hasSize(2);
        assertThat(history.getFirst().id()).isEqualTo(fixedUuid(3));
        assertThat(history.getFirst().type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(history.getFirst().balanceAfter()).isEqualByComparingTo("60.00");
        assertThat(history.getLast().id()).isEqualTo(fixedUuid(2));
        assertThat(history.getLast().type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.getLast().balanceAfter()).isEqualByComparingTo("100.00");
    }

    @Test
    void historyIsScopedToOneAccount() {
        var other = createAccount.invoke(new CreateAccountCommand("Grace", "Hopper")).id();
        record(TransactionType.DEPOSIT, "5.00");

        assertThat(getTransactionHistory.invoke(new TransactionHistoryQuery(other))).isEmpty();
        assertThat(getTransactionHistory.invoke(new TransactionHistoryQuery(accountId))).hasSize(1);
    }

    @Test
    void unknownAccountIsRejected() {
        var unknown = UUID.randomUUID();

        assertThatThrownBy(() -> getTransactionHistory.invoke(new TransactionHistoryQuery(unknown)))
                .isInstanceOf(AccountNotFoundException.class);
    }

    private void record(TransactionType type, String amount) {
        recordTransaction.invoke(new RecordTransactionCommand(accountId, type, new BigDecimal(amount)));
    }
}
