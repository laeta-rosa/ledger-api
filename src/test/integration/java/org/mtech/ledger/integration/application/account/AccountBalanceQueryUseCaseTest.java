package org.mtech.ledger.integration.application.account;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class AccountBalanceQueryUseCaseTest {

    private final AccountBalanceQueryUseCase getAccountBalance;

    @Test
    void unknownAccountIsRejected() {
        var unknown = UUID.randomUUID();

        assertThatThrownBy(() -> getAccountBalance.invoke(new AccountBalanceQuery(unknown)))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
