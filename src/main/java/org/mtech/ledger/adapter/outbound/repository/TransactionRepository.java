package org.mtech.ledger.adapter.outbound.repository;

import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.springframework.data.repository.CrudRepository;

/**
 * Outbound repository adapter for transactions. Spring Data JDBC generates the
 * implementation, so no separate port interface is needed — the application
 * layer depends on this adapter directly.
 */
public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    /**
     * Transactions for an account, newest first. The {@code id} is a secondary
     * sort key so that movements sharing a timestamp (same clock tick) still have
     * a stable, deterministic order rather than one the database picks arbitrarily.
     */
    List<Transaction> findByAccountIdOrderByTimestampDescIdDesc(UUID accountId);
}
