package org.mtech.ledger.application.transaction.record;

import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.adapter.outbound.repository.TransactionRepository;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.common.usecase.CommandUseCase;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.transaction.Transaction;
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

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    @Override
    @Transactional
    public TransactionResult invoke(RecordTransactionCommand command) {
        var account = accounts.findById(command.accountId())
                .orElseThrow(() -> new AccountNotFoundException(command.accountId()));
        var normalized = command.amount().setScale(2, RoundingMode.UNNECESSARY);

        command.type().apply(account, normalized);
        accounts.save(account);

        var transaction = new Transaction(
                UUID.randomUUID(),
                command.accountId(),
                command.type(),
                normalized,
                Instant.now(),
                account.getBalance());
        return TransactionResult.from(transactions.save(transaction));
    }
}
