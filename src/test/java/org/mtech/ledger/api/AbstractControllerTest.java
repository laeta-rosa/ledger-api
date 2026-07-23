package org.mtech.ledger.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.mtech.ledger.harness.DatabaseTestHarness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

public abstract class AbstractControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    protected DatabaseTestHarness db;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
    }

    /** Creates an account and returns its id, asserting the 201 + zero starting balance. */
    protected String createAccount() {
        return given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Ada\",\"surname\":\"Lovelace\"}")
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .body("balance", equalTo(0.00f))
                .extract()
                .path("id");
    }
}