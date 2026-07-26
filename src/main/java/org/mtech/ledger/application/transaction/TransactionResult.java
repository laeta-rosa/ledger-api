package org.mtech.ledger.application.transaction;

import java.time.Instant;
import java.util.List;

import org.mtech.ledger.domain.transaction.Transaction;
import org.mtech.ledger.domain.transaction.TransactionType;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.mtech.ledger.domain.vo.TransactionId;

/**
 * The result of a transaction command, decoupling adapters from the domain record.
 */
public sealed interface TransactionResult {

    record Success(List<FoundTransaction> transactions) implements TransactionResult {

        public static Success of(Transaction transaction) {
            return new Success(List.of(FoundTransaction.of(transaction)));
        }

        public record FoundTransaction(
                TransactionId id,
                TransactionType type,
                Money amount,
                Instant timestamp,
                Money balanceAfter) {

            public static FoundTransaction of(Transaction transaction) {
                return new FoundTransaction(
                        transaction.id(),
                        transaction.type(),
                        transaction.amount(),
                        transaction.timestamp(),
                        transaction.balanceAfter());
            }
        }
    }

    record AccountNotFound(AccountId accountId) implements TransactionResult {
    }
}
