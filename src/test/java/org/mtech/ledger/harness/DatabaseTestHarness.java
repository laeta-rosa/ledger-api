package org.mtech.ledger.harness;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reads persisted state directly from the H2 database so integration tests can assert
 * what actually landed in the tables, independent of the HTTP responses.
 */
@Component
@RequiredArgsConstructor
public class DatabaseTestHarness {

    private final JdbcTemplate jdbc;

    /** A persisted {@code account} row, narrowed to the columns tests assert on. */
    public record AccountRow(String name, String surname, BigDecimal balance) {}

    /** A persisted {@code account_transaction} row, narrowed to the columns tests assert on. */
    public record TransactionRow(String type, BigDecimal amount, BigDecimal balanceAfter) {}

    /** The total number of persisted accounts. */
    public int accountCount() {
        return jdbc.queryForObject("SELECT COUNT(*) FROM account", Integer.class);
    }

    /** The stored account holder and balance. */
    public AccountRow accountOf(String accountId) {
        return jdbc.queryForObject(
                "SELECT name, surname, balance FROM account WHERE id = ?",
                (rs, rowNum) -> new AccountRow(
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getBigDecimal("balance")),
                UUID.fromString(accountId));
    }

    /** The stored balance of the given account. */
    public BigDecimal balanceOf(String accountId) {
        return jdbc.queryForObject(
                "SELECT balance FROM account WHERE id = ?",
                BigDecimal.class,
                UUID.fromString(accountId));
    }

    /** The account's transactions, oldest first. */
    public List<TransactionRow> transactionsOf(String accountId) {
        return jdbc.query(
                "SELECT type, amount, balance_after FROM account_transaction "
                        + "WHERE account_id = ? ORDER BY timestamp, id",
                (rs, rowNum) -> new TransactionRow(
                        rs.getString("type"),
                        rs.getBigDecimal("amount"),
                        rs.getBigDecimal("balance_after")),
                UUID.fromString(accountId));
    }
}