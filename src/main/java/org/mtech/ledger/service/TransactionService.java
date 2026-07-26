package org.mtech.ledger.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.vo.TransactionId;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Money movements: recording deposits/withdrawals and reading history. */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountService accounts;
    private final TransactionRepository transactions;

    /**
     * Records a deposit or withdrawal atomically: the balance update and the
     * appended transaction commit together. The account's optimistic-lock
     * version guards against lost updates from concurrent movements, so an
     * overdraft can never slip through a race.
     */
    @Transactional
    public Transaction record(AccountId accountId, TransactionType transactionType, BigDecimal amount) {
        var account = accounts.getAccount(accountId);
        var money = Money.of(amount);

        transactionType.apply(account, money);

        accounts.save(account);

        var transaction = new Transaction(
                TransactionId.random(), accountId, transactionType, money, Instant.now(), account.getBalance());
        return transactions.save(transaction);
    }

    @Transactional(readOnly = true)
    public List<Transaction> getHistory(AccountId accountId) {
        accounts.getAccount(accountId);
        return transactions.findByAccountIdNewestFirst(accountId);
    }
}