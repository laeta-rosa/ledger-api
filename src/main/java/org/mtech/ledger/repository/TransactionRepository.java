package org.mtech.ledger.repository;

import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    /**
     * Transactions for an account, newest first. The {@code id} is a secondary
     * sort key so that movements sharing a timestamp (same clock tick) still have
     * a stable, deterministic order rather than one the database picks arbitrarily.
     */
    List<Transaction> findByAccountIdOrderByTimestampDescIdDesc(UUID accountId);
}
