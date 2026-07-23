package org.mtech.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.meta.IntegrationTest;
import org.springframework.dao.OptimisticLockingFailureException;

@IntegrationTest
@RequiredArgsConstructor
class AccountServiceTest {

    private final AccountService accounts;

    @Test
    void newAccountHasZeroBalance() {
        var account = accounts.createAccount("Ada", "Lovelace");

        assertThat(accounts.getBalance(account.getId())).isEqualByComparingTo("0.00");
    }

    @Test
    void unknownAccountIsRejected() {
        var unknown = UUID.randomUUID();

        assertThatThrownBy(() -> accounts.getBalance(unknown))
                .isInstanceOf(AccountNotFoundException.class);
        assertThatThrownBy(() -> accounts.getAccount(unknown))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void concurrentModificationIsRejected() {
        var accountId = accounts.createAccount("Ada", "Lovelace").getId();

        // Two callers read the account at the same version.
        var first = accounts.getAccount(accountId);
        var second = accounts.getAccount(accountId);

        // The first write wins and bumps the version.
        first.deposit(new BigDecimal("100.00"));
        accounts.save(first);

        // The second write is now stale: the optimistic lock must reject it rather
        // than let the balance silently roll back to a lost-update value. This is the
        // guarantee that stops an overdraft from slipping through concurrent withdrawals.
        second.deposit(new BigDecimal("50.00"));
        assertThatThrownBy(() -> accounts.save(second))
                .isInstanceOf(OptimisticLockingFailureException.class);

        assertThat(accounts.getBalance(accountId)).isEqualByComparingTo("100.00");
    }
}