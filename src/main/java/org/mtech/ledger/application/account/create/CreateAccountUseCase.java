package org.mtech.ledger.application.account.create;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.common.usecase.CommandUseCase;
import org.mtech.ledger.domain.account.Account;
import org.springframework.stereotype.Service;

/** Command use case: opens a new account with a zero starting balance. */
@Service
@RequiredArgsConstructor
public class CreateAccountUseCase implements CommandUseCase<CreateAccountCommand, AccountResult> {

    private final AccountRepository accounts;

    @Override
    public AccountResult invoke(CreateAccountCommand command) {
        var account = accounts.save(Account.open(command.name(), command.surname()));
        return AccountResult.from(account);
    }
}
