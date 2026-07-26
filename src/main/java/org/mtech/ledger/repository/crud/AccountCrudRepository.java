package org.mtech.ledger.repository.crud;

import java.util.UUID;
import org.mtech.ledger.repository.entity.AccountEntity;
import org.springframework.data.repository.CrudRepository;

public interface AccountCrudRepository extends CrudRepository<AccountEntity, UUID> {
}
