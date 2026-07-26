package org.mtech.ledger.repository;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.repository.crud.AccountCrudRepository;
import org.mtech.ledger.repository.entity.AccountEntity;
import org.springframework.stereotype.Repository;

/**
 * Persistence boundary for accounts: speaks the domain language outward
 * ({@link Account}, {@link AccountId}) and maps to {@link AccountEntity} rows
 * through the Spring Data JDBC repository underneath.
 */
@Repository
@RequiredArgsConstructor
public class AccountRepository {

    private final AccountCrudRepository accounts;

    public Account save(Account account) {
        return accounts.save(AccountEntity.from(account)).toDomain();
    }

    public Optional<Account> findById(AccountId accountId) {
        return accounts.findById(accountId.value()).map(AccountEntity::toDomain);
    }
}
