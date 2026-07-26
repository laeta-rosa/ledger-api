package org.mtech.ledger.application.transaction.record;

import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.adapter.outbound.repository.TransactionRepository;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.application.transaction.TransactionResult.AccountNotFound;
import org.mtech.ledger.application.transaction.TransactionResult.Success;
import org.mtech.ledger.common.usecase.CommandUseCase;
import org.mtech.ledger.common.uuid.UuidGenerator;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.vo.TransactionId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command use case: records a deposit or withdrawal atomically. The balance
 * update and the appended transaction commit together; the account's
 * optimistic-lock version guards against lost updates from concurrent
 * movements, so an overdraft can never slip through a race.
 */
@Service
@RequiredArgsConstructor
public class RecordTransactionUseCase implements CommandUseCase<RecordTransactionCommand, TransactionResult> {

    private final Clock clock;
    private final AccountRepository accounts;
    private final UuidGenerator uuidGenerator;
    private final TransactionRepository transactions;

    @Override
    @Transactional
    public TransactionResult invoke(RecordTransactionCommand command) {
        return accounts.findById(command.accountId())
                .<TransactionResult>map(account -> record(account, command))
                .orElseGet(() -> new AccountNotFound(command.accountId()));
    }

    private Success record(Account account, RecordTransactionCommand command) {
        command.type().apply(account, command.amount());
        accounts.save(account);

        var transaction = new Transaction(
                TransactionId.of(uuidGenerator.generate()),
                account.getId(),
                command.type(),
                command.amount(),
                clock.instant(),
                account.getBalance());
        return Success.of(transactions.save(transaction));
    }
}
