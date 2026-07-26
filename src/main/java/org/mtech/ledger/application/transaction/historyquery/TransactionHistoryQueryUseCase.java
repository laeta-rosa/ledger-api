package org.mtech.ledger.application.transaction.historyquery;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.adapter.outbound.repository.TransactionRepository;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.application.transaction.TransactionResult.AccountNotFound;
import org.mtech.ledger.application.transaction.TransactionResult.Success;
import org.mtech.ledger.application.transaction.TransactionResult.Success.FoundTransaction;
import org.mtech.ledger.common.usecase.QueryUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query use case: returns an account's transactions, newest first, reporting a missing account as a result. */
@Service
@RequiredArgsConstructor
public class TransactionHistoryQueryUseCase implements QueryUseCase<TransactionHistoryQuery, TransactionResult> {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    @Override
    @Transactional(readOnly = true)
    public TransactionResult invoke(TransactionHistoryQuery query) {
        if (!accounts.existsById(query.accountId())) {
            return new AccountNotFound(query.accountId());
        }
        return new Success(transactions.findByAccountIdNewestFirst(query.accountId()).stream()
                .map(FoundTransaction::of)
                .toList());
    }
}
