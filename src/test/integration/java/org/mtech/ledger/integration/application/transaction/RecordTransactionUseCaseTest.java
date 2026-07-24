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
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.application.transaction.record.RecordTransactionCommand;
import org.mtech.ledger.application.transaction.record.RecordTransactionUseCase;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.account.InsufficientFundsException;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.integration.harness.FixedUuidGenerator;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class RecordTransactionUseCaseTest {

    private final CreateAccountUseCase createAccount;
    private final RecordTransactionUseCase recordTransaction;
    private final AccountBalanceQueryUseCase getAccountBalance;
    private final FixedUuidGenerator uuids;

    private UUID accountId;

    @BeforeEach
    void setUp() {
        uuids.reset();
        accountId = createAccount.invoke(new CreateAccountCommand("Ada", "Lovelace")).id();
    }

    @Test
    void depositIncreasesBalance() {
        var tx = record(TransactionType.DEPOSIT, "100.50");

        assertThat(tx.id()).isEqualTo(fixedUuid(2));
        assertThat(tx.balanceAfter()).isEqualByComparingTo("100.50");
        assertThat(balance()).isEqualByComparingTo("100.50");
    }

    @Test
    void withdrawalDecreasesBalance() {
        record(TransactionType.DEPOSIT, "100.00");
        record(TransactionType.WITHDRAWAL, "30.00");

        assertThat(balance()).isEqualByComparingTo("70.00");
    }

    @Test
    void withdrawalBeyondBalanceIsRejected() {
        record(TransactionType.DEPOSIT, "10.00");

        assertThatThrownBy(() -> record(TransactionType.WITHDRAWAL, "10.01"))
                .isInstanceOf(InsufficientFundsException.class);
        assertThat(balance()).isEqualByComparingTo("10.00");
    }

    @Test
    void unknownAccountIsRejected() {
        var unknown = UUID.randomUUID();

        assertThatThrownBy(() -> recordTransaction.invoke(
                        new RecordTransactionCommand(unknown, TransactionType.DEPOSIT, new BigDecimal("1.00"))))
                .isInstanceOf(AccountNotFoundException.class);
    }

    private TransactionResult record(TransactionType type, String amount) {
        return recordTransaction.invoke(
                new RecordTransactionCommand(accountId, type, new BigDecimal(amount)));
    }

    private BigDecimal balance() {
        return getAccountBalance.invoke(new AccountBalanceQuery(accountId)).balance();
    }
}
