package org.mtech.ledger.service;

import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.account.AccountNotFoundException;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.repository.AccountRepository;
import org.springframework.stereotype.Service;

/** Account lifecycle: opening accounts and reading their state. */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accounts;

    public Account createAccount(String name, String surname) {
        return accounts.save(Account.open(name, surname));
    }

    public Money getBalance(AccountId accountId) {
        return getAccount(accountId).getBalance();
    }

    public Account getAccount(AccountId accountId) {
        return accounts.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public void save(Account account) {
        accounts.save(account);
    }
}