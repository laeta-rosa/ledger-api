package org.mtech.ledger.application.transaction.historyquery;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.adapter.outbound.repository.TransactionRepository;
import org.mtech.ledger.application.transaction.TransactionResult;
import org.mtech.ledger.common.usecase.QueryUseCase;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Query use case: returns an account's transactions, newest first. */
@Service
@RequiredArgsConstructor
public class TransactionHistoryQueryUseCase implements QueryUseCase<TransactionHistoryQuery, List<TransactionResult>> {

    private final AccountRepository accounts;
    private final TransactionRepository transactions;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResult> invoke(TransactionHistoryQuery query) {
        if (!accounts.existsById(query.accountId())) {
            throw new AccountNotFoundException(query.accountId());
        }
        return transactions.findByAccountIdOrderByTimestampDescIdDesc(query.accountId()).stream()
                .map(TransactionResult::from)
                .toList();
    }
}
