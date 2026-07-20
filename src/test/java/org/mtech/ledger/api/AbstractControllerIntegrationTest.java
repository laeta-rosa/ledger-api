package org.mtech.ledger.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Boots the full application on a random port and drives it over real HTTP with
 * REST Assured. Shared setup and the {@code createAccount} helper live here so the
 * per-controller test classes stay focused on their own endpoints.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class AbstractControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void configureRestAssured() {
        RestAssured.port = port;
    }

    /** Creates an account and returns its id, asserting the 201 + zero starting balance. */
    protected String createAccount() {
        return given()
                .when()
                .post("/accounts")
                .then()
                .statusCode(201)
                .body("balance", equalTo(0.00f))
                .extract()
                .path("id");
    }
}