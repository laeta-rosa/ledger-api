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
                .body("name", equalTo("Ada"))
                .body("surname", equalTo("Lovelace"))
                .body("balance", equalTo(0.00f));
    }

    @Test
    void createReturnsAccountHolderName() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Grace\",\"surname\":\"Hopper\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .body("name", equalTo("Grace"))
                .body("surname", equalTo("Hopper"));
    }

    @Test
    void overlongNameIsRejected() {
        var tooLong = "A".repeat(101);

        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + tooLong + "\",\"surname\":\"Hopper\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(400);
    }

    @Test
    void blankNameIsRejected() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"\",\"surname\":\"Hopper\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(400);
    }

    @Test
    void balanceReflectsRecordedMovements() {
        var accountId = createAccount();

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":100.00}")
                .when()
                .post("/accounts/{id}/deposit", accountId)
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body("{\"amount\":30.00}")
                .when()
                .post("/accounts/{id}/withdrawal", accountId)
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