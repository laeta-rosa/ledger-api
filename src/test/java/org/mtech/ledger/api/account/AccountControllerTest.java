package org.mtech.ledger.api.account;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.http.ContentType;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.mtech.ledger.api.AbstractControllerTest;
import org.mtech.ledger.harness.DatabaseTestHarness.AccountRow;
import org.mtech.ledger.meta.IntegrationTest;

/** HTTP tests for {@link AccountController}: account creation and balance. */
@IntegrationTest
class AccountControllerTest extends AbstractControllerTest {

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

        assertThat(db.accountOf(accountId))
                .isEqualTo(new AccountRow("Ada", "Lovelace", new BigDecimal("0.00")));
    }

    @Test
    void createReturnsAccountHolderName() {
        String accountId = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Grace\",\"surname\":\"Hopper\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .body("name", equalTo("Grace"))
                .body("surname", equalTo("Hopper"))
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId))
                .isEqualTo(new AccountRow("Grace", "Hopper", new BigDecimal("0.00")));
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

        assertThat(db.accountCount()).isZero();
    }

    @Test
    void maxLengthNameIsAccepted() {
        var maxName = "A".repeat(100);

        String accountId = given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"" + maxName + "\",\"surname\":\"Hopper\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId))
                .isEqualTo(new AccountRow(maxName, "Hopper", new BigDecimal("0.00")));
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

        assertThat(db.accountCount()).isZero();
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

        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("70.00");
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