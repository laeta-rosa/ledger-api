package org.mtech.ledger.repository.crud;

import java.util.List;
import java.util.UUID;
import org.mtech.ledger.repository.entity.TransactionEntity;
import org.springframework.data.repository.CrudRepository;

public interface TransactionCrudRepository extends CrudRepository<TransactionEntity, UUID> {

    List<TransactionEntity> findByAccountIdOrderByTimestampDescIdDesc(UUID accountId);
}
