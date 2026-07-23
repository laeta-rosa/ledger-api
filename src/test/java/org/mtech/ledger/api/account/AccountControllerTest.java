package org.mtech.ledger.api.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

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

        rest.get("/accounts/{id}/balance", accountId)
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
        String accountId = rest.post("/accounts", "{\"name\":\"Grace\",\"surname\":\"Hopper\"}")
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

        rest.post("/accounts", "{\"name\":\"" + tooLong + "\",\"surname\":\"Hopper\"}")
                .statusCode(400);

        assertThat(db.accountCount()).isZero();
    }

    @Test
    void maxLengthNameIsAccepted() {
        var maxName = "A".repeat(100);

        String accountId = rest.post("/accounts", "{\"name\":\"" + maxName + "\",\"surname\":\"Hopper\"}")
                .statusCode(201)
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId))
                .isEqualTo(new AccountRow(maxName, "Hopper", new BigDecimal("0.00")));
    }

    @Test
    void blankNameIsRejected() {
        rest.post("/accounts", "{\"name\":\"\",\"surname\":\"Hopper\"}")
                .statusCode(400);

        assertThat(db.accountCount()).isZero();
    }

    @Test
    void balanceReflectsRecordedMovements() {
        var accountId = createAccount();

        rest.post("/accounts/{id}/deposit", "{\"amount\":100.00}", accountId)
                .statusCode(201);

        rest.post("/accounts/{id}/withdrawal", "{\"amount\":30.00}", accountId)
                .statusCode(201);

        rest.get("/accounts/{id}/balance", accountId)
                .statusCode(200)
                .body("balance", equalTo(70.00f));

        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("70.00");
    }

    @Test
    void unknownAccountReturns404() {
        rest.get("/accounts/{id}/balance", "00000000-0000-0000-0000-000000000000")
                .statusCode(404);
    }
}