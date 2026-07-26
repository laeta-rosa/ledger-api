package org.mtech.ledger.application.account.create;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.application.account.AccountResult;
import org.mtech.ledger.application.account.AccountResult.Success;
import org.mtech.ledger.common.usecase.CommandUseCase;
import org.mtech.ledger.common.uuid.UuidGenerator;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.AccountId;
import org.springframework.stereotype.Service;

/** Command use case: opens a new account with a zero starting balance. */
@Service
@RequiredArgsConstructor
public class CreateAccountUseCase implements CommandUseCase<CreateAccountCommand, AccountResult.Success> {

    private final AccountRepository accounts;
    private final UuidGenerator uuidGenerator;

    @Override
    public Success invoke(CreateAccountCommand command) {
        var account = accounts.save(
                Account.open(AccountId.of(uuidGenerator.generate()), command.name(), command.surname()));
        return Success.of(account);
    }
}
