package org.mtech.ledger.application.account.balancequery;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.common.usecase.QueryUseCase;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.springframework.stereotype.Service;

/** Query use case: reads an account's current state or fails if it does not exist. */
@Service
@RequiredArgsConstructor
public class AccountBalanceQueryUseCase implements QueryUseCase<AccountBalanceQuery, AccountResult> {

    private final AccountRepository accounts;

    @Override
    public AccountResult invoke(AccountBalanceQuery query) {
        var account = accounts.findById(query.accountId())
                .orElseThrow(() -> new AccountNotFoundException(query.accountId()));
        return AccountResult.from(account);
    }
}
