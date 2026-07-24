package org.mtech.ledger.integration.harness;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import io.restassured.response.ValidatableResponse;
import org.mtech.ledger.integration.fixture.TestMessage;
import org.springframework.stereotype.Component;

/**
 * Issues the HTTP calls integration tests make.
 */
@Component
public class RestTestHarness {

    /** GET {@code path}, substituting {@code pathParams} into its {@code {...}} placeholders. */
    public ValidatableResponse get(String path, Object... pathParams) {
        return given()
                .when()
                .get(path, pathParams)
                .then();
    }

    /** POST {@code body}'s JSON to {@code path}, substituting {@code pathParams}. */
    public ValidatableResponse post(String path, TestMessage body, Object... pathParams) {
        return post(path, body.asJson(), pathParams);
    }

    /** POST {@code body} as JSON to {@code path}, substituting {@code pathParams}. */
    public ValidatableResponse post(String path, String body, Object... pathParams) {
        return given()
                .contentType(JSON)
                .body(body)
                .when()
                .post(path, pathParams)
                .then();
    }
}
