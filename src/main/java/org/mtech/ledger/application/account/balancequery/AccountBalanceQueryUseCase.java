package org.mtech.ledger.application.account.balancequery;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.application.account.AccountResult.NotFound;
import org.mtech.ledger.application.account.AccountResult.Success;
import org.mtech.ledger.common.usecase.QueryUseCase;
import org.springframework.stereotype.Service;

/** Query use case: reads an account's current state, reporting a missing account as a result. */
@Service
@RequiredArgsConstructor
public class AccountBalanceQueryUseCase implements QueryUseCase<AccountBalanceQuery, AccountResult> {

    private final AccountRepository accounts;

    @Override
    public AccountResult invoke(AccountBalanceQuery query) {
        return accounts.findById(query.accountId())
                .<AccountResult>map(Success::of)
                .orElseGet(() -> new NotFound(query.accountId()));
    }
}
