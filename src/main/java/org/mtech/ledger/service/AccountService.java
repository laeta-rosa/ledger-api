package org.mtech.ledger.service;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.Account;
import org.mtech.ledger.domain.AccountNotFoundException;
import org.mtech.ledger.repository.AccountRepository;
import org.springframework.stereotype.Service;

/** Account lifecycle: opening accounts and reading their state. */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accounts;

    public Account createAccount() {
        return accounts.save(Account.open());
    }

    public BigDecimal getBalance(UUID accountId) {
        return getAccount(accountId).getBalance();
    }

    /** Loads an account or throws {@link AccountNotFoundException}. */
    public Account getAccount(UUID accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    /** Persists balance/version changes made to an account. */
    public Account save(Account account) {
        return accounts.save(account);
    }
}