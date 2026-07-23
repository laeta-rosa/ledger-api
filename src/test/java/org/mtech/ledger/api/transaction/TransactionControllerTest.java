package org.mtech.ledger.api.transaction;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
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

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":100.00}")
                .when()
                .post("/accounts/{id}/deposit", accountId)
                .then()
                .statusCode(201)
                .body("type", equalTo("DEPOSIT"))
                .body("balanceAfter", equalTo(100.00f));

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":30.00}")
                .when()
                .post("/accounts/{id}/withdrawal", accountId)
                .then()
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

        given()
                .when()
                .get("/accounts/{id}/transactions", accountId)
                .then()
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

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":1.00}")
                .when()
                .post("/accounts/{id}/withdrawal", accountId)
                .then()
                .statusCode(422);

        assertThat(db.transactionsOf(accountId)).isEmpty();
        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("0.00");
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5.00", "0", "1.005", "1000000000000"})
    void invalidAmountReturns400(String amount) {
        var accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":" + amount + "}")
                .when()
                .post("/accounts/{id}/deposit", accountId)
                .then()
                .statusCode(400);

        assertThat(db.transactionsOf(accountId)).isEmpty();
        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("0.00");
    }

    @Test
    void unknownAccountReturns404() {
        var unknownAccountId = "00000000-0000-0000-0000-000000000000";

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":10.00}")
                .when()
                .post("/accounts/{id}/deposit", unknownAccountId)
                .then()
                .statusCode(404);

        assertThat(db.transactionsOf(unknownAccountId)).isEmpty();
    }

    private void deposit(String accountId, String amount) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":" + amount + "}")
                .when()
                .post("/accounts/{id}/deposit", accountId)
                .then()
                .statusCode(201);
    }

    private void withdraw(String accountId, String amount) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":" + amount + "}")
                .when()
                .post("/accounts/{id}/withdrawal", accountId)
                .then()
                .statusCode(201);
    }
}