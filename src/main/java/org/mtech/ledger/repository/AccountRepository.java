package org.mtech.ledger.repository;

import java.util.UUID;
import org.mtech.ledger.domain.account.Account;
import org.springframework.data.repository.CrudRepository;

public interface AccountRepository extends CrudRepository<Account, UUID> {
}
