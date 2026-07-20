package org.mtech.ledger.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.api.transaction.TransactionController;

/** HTTP tests for {@link TransactionController}: recording movements and history. */
class TransactionControllerIntegrationTest extends AbstractControllerIntegrationTest {

    private void record(String accountId, String type, String amount) {
        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"" + type + "\",\"amount\":" + amount + "}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(201);
    }

    @Test
    void recordsDepositAndWithdrawalWithRunningBalance() {
        String accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"DEPOSIT\",\"amount\":100.00}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(201)
                .body("type", equalTo("DEPOSIT"))
                .body("balanceAfter", equalTo(100.00f));

        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"WITHDRAWAL\",\"amount\":30.00}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(201)
                .body("balanceAfter", equalTo(70.00f));
    }

    @Test
    void historyIsNewestFirst() {
        String accountId = createAccount();
        record(accountId, "DEPOSIT", "100.00");
        record(accountId, "WITHDRAWAL", "30.00");

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
        String accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"WITHDRAWAL\",\"amount\":1.00}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(422);
    }

    @Test
    void invalidAmountReturns400() {
        String accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"DEPOSIT\",\"amount\":-5.00}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(400);
    }

    @Test
    void invalidTypeReturns400() {
        String accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"TRANSFER\",\"amount\":5.00}")
                .when()
                .post("/accounts/{id}/transactions", accountId)
                .then()
                .statusCode(400);
    }

    @Test
    void unknownAccountReturns404() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"DEPOSIT\",\"amount\":10.00}")
                .when()
                .post("/accounts/{id}/transactions", "00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }
}