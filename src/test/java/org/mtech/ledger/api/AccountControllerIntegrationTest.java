package org.mtech.ledger.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.api.account.AccountController;

/** HTTP tests for {@link AccountController}: account creation and balance. */
class AccountControllerIntegrationTest extends AbstractControllerIntegrationTest {

    @Test
    void newAccountStartsWithZeroBalance() {
        var accountId = createAccount();

        given()
                .when()
                .get("/accounts/{id}/balance", accountId)
                .then()
                .statusCode(200)
                .body("id", equalTo(accountId))
                .body("balance", equalTo(0.00f));
    }

    @Test
    void balanceReflectsRecordedMovements() {
        var accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":100.00}")
                .when()
                .post("/accounts/{id}/transactions/deposits", accountId)
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":30.00}")
                .when()
                .post("/accounts/{id}/transactions/withdrawals", accountId)
                .then()
                .statusCode(201);

        given()
                .when()
                .get("/accounts/{id}/balance", accountId)
                .then()
                .statusCode(200)
                .body("balance", equalTo(70.00f));
    }

    @Test
    void unknownAccountReturns404() {
        given()
                .when()
                .get("/accounts/{id}/balance", "00000000-0000-0000-0000-000000000000")
                .then()
                .statusCode(404);
    }
}