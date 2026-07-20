package org.mtech.ledger.repository;

import java.util.List;
import java.util.UUID;
import org.mtech.ledger.domain.transaction.Transaction;
import org.springframework.data.repository.CrudRepository;

public interface TransactionRepository extends CrudRepository<Transaction, UUID> {

    /** Transactions for an account, newest first. */
    List<Transaction> findByAccountIdOrderByTimestampDesc(UUID accountId);
}
