package org.mtech.ledger.adapter.outbound.repository;

import java.util.UUID;
import org.mtech.ledger.domain.account.Account;
import org.springframework.data.repository.CrudRepository;

/**
 * Outbound repository adapter for accounts. Spring Data JDBC generates the
 * implementation, so no separate port interface is needed — the application
 * layer depends on this adapter directly.
 */
public interface AccountRepository extends CrudRepository<Account, UUID> {
}
