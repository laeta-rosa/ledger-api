package org.mtech.ledger.adapter.outbound.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.adapter.outbound.repository.crud.TransactionCrudRepository;
import org.mtech.ledger.adapter.outbound.repository.entity.TransactionEntity;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.vo.AccountId;
import org.springframework.stereotype.Repository;

/**
 * Outbound repository adapter for the append-only transaction ledger: speaks the
 * domain language outward ({@link Transaction}, {@link AccountId}) and maps to
 * {@link TransactionEntity} rows through the Spring Data JDBC repository underneath.
 */
@Repository
@RequiredArgsConstructor
public class TransactionRepository {

    private final TransactionCrudRepository transactions;

    public Transaction save(Transaction transaction) {
        return transactions.save(TransactionEntity.from(transaction)).toDomain();
    }

    public List<Transaction> findByAccountIdNewestFirst(AccountId accountId) {
        return transactions.findByAccountIdOrderByTimestampDescIdDesc(accountId.value()).stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }
}
