package org.mtech.ledger.integration.application.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mtech.ledger.integration.harness.FixedClockConfiguration.FIXED_INSTANT;
import static org.mtech.ledger.integration.harness.FixedUuidGenerator.fixedUuid;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.application.account.create.CreateAccountCommand;
import org.mtech.ledger.application.account.create.CreateAccountUseCase;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.application.transaction.TransactionResult.AccountNotFound;
import org.mtech.ledger.application.transaction.TransactionResult.Success;
import org.mtech.ledger.application.transaction.TransactionResult.Success.FoundTransaction;
import org.mtech.ledger.application.transaction.record.RecordTransactionCommand;
import org.mtech.ledger.application.transaction.record.RecordTransactionUseCase;
import org.mtech.ledger.domain.account.InsufficientFundsException;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.vo.TransactionId;
import org.mtech.ledger.integration.harness.FixedUuidGenerator;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class RecordTransactionUseCaseTest {

    private final FixedUuidGenerator uuids;
    private final CreateAccountUseCase createAccount;
    private final RecordTransactionUseCase recordTransaction;
    private final AccountBalanceQueryUseCase getAccountBalance;

    private AccountId accountId;

    @BeforeEach
    void setUp() {
        uuids.reset();
        accountId = createAccount.invoke(new CreateAccountCommand("Ada", "Lovelace")).id();
    }

    @Test
    void depositIncreasesBalance() {
        var tx = record(TransactionType.DEPOSIT, "100.50");

        assertThat(tx.id()).isEqualTo(TransactionId.of(fixedUuid(2)));
        assertThat(tx.timestamp()).isEqualTo(FIXED_INSTANT);
        assertThat(tx.balanceAfter()).isEqualTo(Money.of("100.50"));
        assertThat(balance()).isEqualTo(Money.of("100.50"));
    }

    @Test
    void withdrawalDecreasesBalance() {
        record(TransactionType.DEPOSIT, "100.00");
        record(TransactionType.WITHDRAWAL, "30.00");

        assertThat(balance()).isEqualTo(Money.of("70.00"));
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        record(TransactionType.DEPOSIT, "10.00");

        assertThatThrownBy(() -> record(TransactionType.WITHDRAWAL, "10.01"))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(balance()).isEqualTo(Money.of("10.00"));
    }

    @Test
    void unknownAccountYieldsNotFound() {
        var unknown = AccountId.of(UUID.randomUUID());

        var result = recordTransaction.invoke(
                new RecordTransactionCommand(unknown, TransactionType.DEPOSIT, Money.of("1.00")));

        assertThat(result).isEqualTo(new AccountNotFound(unknown));
    }

    private FoundTransaction record(TransactionType type, String amount) {
        var result = (Success) recordTransaction.invoke(
                new RecordTransactionCommand(accountId, type, Money.of(amount)));
        return result.transactions().getFirst();
    }

    private Money balance() {
        return ((AccountResult.Success) getAccountBalance.invoke(new AccountBalanceQuery(accountId))).balance();
    }
}
