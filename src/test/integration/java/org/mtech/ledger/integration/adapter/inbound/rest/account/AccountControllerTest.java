package org.mtech.ledger.integration.adapter.inbound.rest.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mtech.ledger.integration.fixture.account.AccountRequest.ADA;
import static org.mtech.ledger.integration.fixture.account.AccountRequest.GRACE;

import org.junit.jupiter.api.Test;
import org.mtech.ledger.integration.fixture.account.AccountRequest;
import org.mtech.ledger.integration.fixture.transaction.TransactionRequest;
import org.mtech.ledger.integration.harness.DatabaseTestHarness.AccountRow;
import org.mtech.ledger.integration.meta.IntegrationTest;
import org.mtech.ledger.integration.adapter.inbound.rest.AbstractControllerTest;

/** HTTP tests for the account controller: account creation and balance. */
@IntegrationTest
class AccountControllerTest extends AbstractControllerTest {

    @Test
    void newAccountStartsWithZeroBalance() {
        var accountId = createAccount();

        rest.get("/accounts/{id}/balance", accountId)
                .statusCode(200)
                .body("id", equalTo(accountId))
                .body("name", equalTo(ADA.name()))
                .body("surname", equalTo(ADA.surname()))
                .body("balance", equalTo(0.00f));

        assertThat(db.accountOf(accountId)).isEqualTo(new AccountRow(ADA.name(), ADA.surname(), ZERO));
    }

    @Test
    void createReturnsAccountHolderName() {
        String accountId = rest.post("/accounts", GRACE)
                .statusCode(201)
                .body("name", equalTo(GRACE.name()))
                .body("surname", equalTo(GRACE.surname()))
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId)).isEqualTo(new AccountRow(GRACE.name(), GRACE.surname(), ZERO));
    }

    @Test
    void overlongNameIsRejected() {
        var tooLong = "A".repeat(101);

        rest.post("/accounts", AccountRequest.withName(tooLong))
                .statusCode(400);

        assertThat(db.accountCount()).isZero();
    }

    @Test
    void maxLengthNameIsAccepted() {
        var maxName = "A".repeat(100);

        String accountId = rest.post("/accounts", AccountRequest.withName(maxName))
                .statusCode(201)
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId)).isEqualTo(new AccountRow(maxName, GRACE.surname(), ZERO));
    }

    @Test
    void surroundingWhitespaceIsTrimmed() {
        var padded = new AccountRequest("   Jane", "Doe   ");

        String accountId = rest.post("/accounts", padded)
                .statusCode(201)
                .body("name", equalTo("Jane"))
                .body("surname", equalTo("Doe"))
                .extract()
                .path("id");

        assertThat(db.accountOf(accountId)).isEqualTo(new AccountRow("Jane", "Doe", ZERO));
    }

    @Test
    void blankNameIsRejected() {
        rest.post("/accounts", AccountRequest.withName(""))
                .statusCode(400);

        assertThat(db.accountCount()).isZero();
    }

    @Test
    void balanceReflectsRecordedMovements() {
        var accountId = createAccount();

        rest.post("/accounts/{id}/deposit", new TransactionRequest("100.00"), accountId)
                .statusCode(201);

        rest.post("/accounts/{id}/withdrawal", new TransactionRequest("30.00"), accountId)
                .statusCode(201);

        rest.get("/accounts/{id}/balance", accountId)
                .statusCode(200)
                .body("balance", equalTo(70.00f));

        assertThat(db.balanceOf(accountId)).isEqualByComparingTo("70.00");
    }

    @Test
    void unknownAccountReturns404() {
        rest.get("/accounts/{id}/balance", UNKNOWN_ACCOUNT_ID)
                .statusCode(404);
    }
}
