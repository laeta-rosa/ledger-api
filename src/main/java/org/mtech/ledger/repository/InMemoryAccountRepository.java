package org.mtech.ledger.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.mtech.ledger.domain.Account;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryAccountRepository {

    private final ConcurrentMap<UUID, Account> accounts = new ConcurrentHashMap<>();

    public Account create() {
        Account account = new Account(UUID.randomUUID());
        accounts.put(account.getId(), account);
        return account;
    }

    public Optional<Account> findById(UUID id) {
        return Optional.ofNullable(accounts.get(id));
    }
}
