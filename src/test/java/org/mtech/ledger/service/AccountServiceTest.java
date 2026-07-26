package org.mtech.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.meta.IntegrationTest;
import org.springframework.dao.OptimisticLockingFailureException;

@IntegrationTest
@RequiredArgsConstructor
class AccountServiceTest {

    private final AccountService accounts;

    @Test
    void newAccountHasZeroBalance() {
        var account = accounts.createAccount("Ada", "Lovelace");

        assertThat(accounts.getBalance(account.getId())).isEqualTo(Money.ZERO);
    }

    @Test
    void unknownAccountIsRejected() {
        var unknown = AccountId.random();

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
        first.deposit(Money.of("100.00"));
        accounts.save(first);

        // The second write is now stale: the optimistic lock must reject it rather
        // than let the balance silently roll back to a lost-update value. This is the
        // guarantee that stops an overdraft from slipping through concurrent withdrawals.
        second.deposit(Money.of("50.00"));
        assertThatThrownBy(() -> accounts.save(second))
                .isInstanceOf(OptimisticLockingFailureException.class);

        assertThat(accounts.getBalance(accountId)).isEqualTo(Money.of("100.00"));
    }
}