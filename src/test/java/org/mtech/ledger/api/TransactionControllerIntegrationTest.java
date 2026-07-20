package org.mtech.ledger.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mtech.ledger.api.transaction.TransactionController;

/** HTTP tests for {@link TransactionController}: recording movements and history. */
class TransactionControllerIntegrationTest extends AbstractControllerIntegrationTest {

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
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5.00", "0", "1.005"})
    void invalidAmountReturns400(String amount) {
        var accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":" + amount + "}")
                .when()
                .post("/accounts/{id}/deposit", accountId)
                .then()
                .statusCode(400);
    }

    @Test
    void unknownAccountReturns404() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":10.00}")
                .when()
                .post("/accounts/{id}/deposit", "00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }
}