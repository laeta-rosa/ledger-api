package org.mtech.ledger.integration.application.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQuery;
import org.mtech.ledger.application.account.balancequery.AccountBalanceQueryUseCase;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.integration.meta.IntegrationTest;

@IntegrationTest
@RequiredArgsConstructor
class AccountBalanceQueryUseCaseTest {

    private final AccountBalanceQueryUseCase getAccountBalance;

    @Test
    void unknownAccountYieldsNotFound() {
        var unknown = AccountId.of(UUID.randomUUID());

        var result = getAccountBalance.invoke(new AccountBalanceQuery(unknown));

        assertThat(result).isEqualTo(new AccountResult.NotFound(unknown));
    }
}
