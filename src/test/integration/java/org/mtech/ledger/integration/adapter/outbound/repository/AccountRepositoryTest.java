package org.mtech.ledger.integration.adapter.outbound.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.adapter.outbound.repository.AccountRepository;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.integration.meta.IntegrationTest;
import org.springframework.dao.OptimisticLockingFailureException;

/**
 * Verifies the optimistic-lock guarantee the account aggregate relies on: a stale write
 * is rejected rather than silently rolling the balance back to a lost-update value. This
 * is what stops an overdraft from slipping through concurrent withdrawals.
 */
@IntegrationTest
@RequiredArgsConstructor
class AccountRepositoryTest {

    private final AccountRepository accounts;

    @Test
    void concurrentModificationIsRejected() {
        var accountId = accounts.save(Account.open("Ada", "Lovelace")).getId();

        // Two callers read the account at the same version.
        var first = accounts.findById(accountId).orElseThrow();
        var second = accounts.findById(accountId).orElseThrow();

        // The first write wins and bumps the version.
        first.deposit(new BigDecimal("100.00"));
        accounts.save(first);

        // The second write is now stale: the optimistic lock must reject it.
        second.deposit(new BigDecimal("50.00"));
        assertThatThrownBy(() -> accounts.save(second))
                .isInstanceOf(OptimisticLockingFailureException.class);

        assertThat(accounts.findById(accountId).orElseThrow().getBalance())
                .isEqualByComparingTo("100.00");
    }
}
