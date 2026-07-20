package org.mtech.ledger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AccountServiceTest {

    @Autowired
    private AccountService accounts;

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
}