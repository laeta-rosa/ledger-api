package org.mtech.ledger.integration.application.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mtech.ledger.integration.harness.FixedUuidGenerator.fixedUuid;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.create.CreateAccountCommand;
import org.mtech.ledger.application.account.create.CreateAccountUseCase;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.integration.harness.FixedUuidGenerator;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class CreateAccountUseCaseTest {

    private final CreateAccountUseCase createAccount;
    private final AccountBalanceQueryUseCase getAccountBalance;
    private final FixedUuidGenerator uuids;

    @BeforeEach
    void setUp() {
        uuids.reset();
    }

    @Test
    void newAccountStartsWithZeroBalance() {
        var created = createAccount.invoke(new CreateAccountCommand("Ada", "Lovelace"));

        assertThat(created.id()).isEqualTo(fixedUuid(1));
        assertThat(created.balance()).isEqualByComparingTo("0.00");

        var balance = getAccountBalance.invoke(new AccountBalanceQuery(created.id()));
        assertThat(balance.balance()).isEqualByComparingTo("0.00");
        assertThat(balance.name()).isEqualTo("Ada");
        assertThat(balance.surname()).isEqualTo("Lovelace");
    }

    @Test
    void trimsHolderName() {
        var created = createAccount.invoke(new CreateAccountCommand("   Jane", "Doe   "));

        assertThat(created.name()).isEqualTo("Jane");
        assertThat(created.surname()).isEqualTo("Doe");
    }
}
