package org.mtech.ledger.adapter.outbound.repository.entity;

import java.math.BigDecimal;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mtech.ledger.domain.account.Account;
import org.mtech.ledger.domain.vo.AccountId;
import org.mtech.ledger.domain.vo.Money;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Database representation of an {@link Account}.
 */
@Table("account")
public record AccountEntity(
        @Id UUID id,
        String name,
        String surname,
        BigDecimal balance,
        @Version @Nullable Long version) {

    public static AccountEntity from(Account account) {
        return new AccountEntity(
                account.getId().value(),
                account.getName(),
                account.getSurname(),
                account.getBalance().value(),
                account.getVersion());
    }

    public Account toDomain() {
        return new Account(AccountId.of(id), name, surname, Money.of(balance), version);
    }
}
