package org.mtech.ledger.domain.account;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Aggregate root for a ledger account. The current balance is the single
 * invariant this aggregate protects, so the balance-changing rules live here.
 * The {@code version} field drives optimistic locking and lets Spring Data JDBC
 * distinguish a freshly opened account (null version) from a persisted one.
 */
@Table("account")
@Getter
@AllArgsConstructor
public class Account {

    @Id
    private final UUID id;

    private final String name;

    private final String surname;

    private BigDecimal balance;

    @Version
    private Long version;

    public static Account open(String name, String surname) {
        return new Account(UUID.randomUUID(), name.strip(), surname.strip(), new BigDecimal("0.00"), null);
    }

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        var newBalance = this.balance.subtract(amount);
        if (newBalance.signum() < 0) {
            throw new InsufficientFundsException(this.balance, amount);
        }
        this.balance = newBalance;
    }
}
