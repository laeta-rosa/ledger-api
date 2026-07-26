package org.mtech.ledger.integration.application.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mtech.ledger.integration.harness.FixedUuidGenerator.fixedUuid;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.create.CreateAccountCommand;
import org.mtech.ledger.application.account.create.CreateAccountUseCase;
import org.mtech.ledger.application.transaction.TransactionResult.AccountNotFound;
import org.mtech.ledger.application.transaction.TransactionResult.Success;
import org.mtech.ledger.application.transaction.TransactionResult.Success.FoundTransaction;
import org.mtech.ledger.application.transaction.historyquery.TransactionHistoryQuery;
import org.mtech.ledger.application.transaction.historyquery.TransactionHistoryQueryUseCase;
import org.mtech.ledger.application.transaction.record.RecordTransactionCommand;
import org.mtech.ledger.application.transaction.record.RecordTransactionUseCase;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.vo.TransactionId;
import org.mtech.ledger.integration.harness.FixedUuidGenerator;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class TransactionHistoryQueryUseCaseTest {

    private final CreateAccountUseCase createAccount;
    private final RecordTransactionUseCase recordTransaction;
    private final TransactionHistoryQueryUseCase getTransactionHistory;
    private final FixedUuidGenerator uuids;

    private AccountId accountId;

    @BeforeEach
    void setUp() {
        uuids.reset();
        accountId = createAccount.invoke(new CreateAccountCommand("Ada", "Lovelace")).id();
    }

    @Test
    void historyIsNewestFirstAndTracksBalance() {
        record(TransactionType.DEPOSIT, "100.00");
        record(TransactionType.WITHDRAWAL, "40.00");

        var history = history(accountId);

        assertThat(history).hasSize(2);
        assertThat(history.getFirst().id()).isEqualTo(TransactionId.of(fixedUuid(3)));
        assertThat(history.getFirst().type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(history.getFirst().balanceAfter()).isEqualTo(Money.of("60.00"));
        assertThat(history.getLast().id()).isEqualTo(TransactionId.of(fixedUuid(2)));
        assertThat(history.getLast().type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.getLast().balanceAfter()).isEqualTo(Money.of("100.00"));
    }

    @Test
    void historyIsScopedToOneAccount() {
        var other = createAccount.invoke(new CreateAccountCommand("Grace", "Hopper")).id();
        record(TransactionType.DEPOSIT, "5.00");

        assertThat(history(other)).isEmpty();
        assertThat(history(accountId)).hasSize(1);
    }

    @Test
    void unknownAccountYieldsNotFound() {
        var unknown = AccountId.of(UUID.randomUUID());

        var result = getTransactionHistory.invoke(new TransactionHistoryQuery(unknown));

        assertThat(result).isEqualTo(new AccountNotFound(unknown));
    }

    private void record(TransactionType type, String amount) {
        recordTransaction.invoke(new RecordTransactionCommand(accountId, type, Money.of(amount)));
    }

    private List<FoundTransaction> history(AccountId id) {
        return ((Success) getTransactionHistory.invoke(new TransactionHistoryQuery(id))).transactions();
    }
}
