package org.mtech.ledger.integration.adapter.inbound.rest;

import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.mtech.ledger.integration.fixture.account.AccountRequest;
import org.mtech.ledger.integration.harness.DatabaseTestHarness;
import org.mtech.ledger.integration.harness.RestTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class AbstractControllerTest {

    /** An account id that is well-formed but never persisted, for not-found paths. */
    protected static final String UNKNOWN_ACCOUNT_ID = "00000000-0000-0000-0000-000000000000";

    protected static final BigDecimal ZERO = new BigDecimal("0.00");

    @LocalServerPort
    private int port;

    @Autowired
    protected RestTestHarness rest;

    @Autowired
    protected DatabaseTestHarness db;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
    }

    /** Creates an account and returns its id, asserting the 201 + zero starting balance. */
    protected String createAccount() {
        return rest.post("/accounts", AccountRequest.ADA)
                .statusCode(201)
                .body("balance", equalTo(0.00f))
                .extract()
                .path("id");
    }
}
