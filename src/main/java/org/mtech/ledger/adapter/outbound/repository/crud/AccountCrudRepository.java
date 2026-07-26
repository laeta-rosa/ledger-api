package org.mtech.ledger.adapter.outbound.repository.crud;

import java.util.UUID;
import org.mtech.ledger.adapter.outbound.repository.entity.AccountEntity;
import org.springframework.data.repository.CrudRepository;

public interface AccountCrudRepository extends CrudRepository<AccountEntity, UUID> {
}
