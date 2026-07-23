package org.mtech.ledger.api.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mtech.ledger.api.AbstractControllerTest;
import org.mtech.ledger.harness.DatabaseTestHarness.TransactionRow;
import org.mtech.ledger.meta.IntegrationTest;

/** HTTP tests for {@link TransactionController}: recording movements and history. */
@IntegrationTest
class TransactionControllerTest extends AbstractControllerTest {

    @Test
    void recordsDepositAndWithdrawalWithRunningBalance() {
        var accountId = createAccount();

        rest.post("/accounts/{id}/deposit", "{\"amount\":100.00}", accountId)
                .statusCode(201)
                .body("type", equalTo("DEPOSIT"))
                .body("balanceAfter", equalTo(100.00f));

        rest.post("/accounts/{id}/withdrawal", "{\"amount\":30.00}", accountId)
                .statusCode(201)
                .body("type", equalTo("WITHDRAWAL"))
                .body("balanceAfter", equalTo(70.00f));

        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("70.00");
        assertThat(db.transactionsOf(accountId))
                .containsExactly(
                        new TransactionRow("DEPOSIT", new BigDecimal("100.00"), new BigDecimal("100.00")),
                        new TransactionRow("WITHDRAWAL", new BigDecimal("30.00"), new BigDecimal("70.00")));
    }

    @Test
    void historyIsNewestFirst() {
        var accountId = createAccount();
        deposit(accountId, "100.00");
        withdraw(accountId, "30.00");

        rest.get("/accounts/{id}/transactions", accountId)
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].type", equalTo("WITHDRAWAL"))
                .body("[1].type", equalTo("DEPOSIT"));

        assertThat(db.transactionsOf(accountId))
                .extracting(TransactionRow::type)
                .containsExactly("DEPOSIT", "WITHDRAWAL");
    }

    @Test
    void overdraftReturns422() {
        var accountId = createAccount();

        rest.post("/accounts/{id}/withdrawal", "{\"amount\":1.00}", accountId)
                .statusCode(422);

        assertThat(db.transactionsOf(accountId)).isEmpty();
        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("0.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5.00", "0", "1.005", "1000000000000"})
    void invalidAmountReturns400(String amount) {
        var accountId = createAccount();

        rest.post("/accounts/{id}/deposit", "{\"amount\":" + amount + "}", accountId)
                .statusCode(400);

        assertThat(db.transactionsOf(accountId)).isEmpty();
        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("0.00");
    }

    @Test
    void unknownAccountReturns404() {
        var unknownAccountId = "00000000-0000-0000-0000-000000000000";

        rest.post("/accounts/{id}/deposit", "{\"amount\":10.00}", unknownAccountId)
                .statusCode(404);

        assertThat(db.transactionsOf(unknownAccountId)).isEmpty();
    }

    private void deposit(String accountId, String amount) {
        rest.post("/accounts/{id}/deposit", "{\"amount\":" + amount + "}", accountId)
                .statusCode(201);
    }

    private void withdraw(String accountId, String amount) {
        rest.post("/accounts/{id}/withdrawal", "{\"amount\":" + amount + "}", accountId)
                .statusCode(201);
    }
}