package org.mtech.ledger.adapter.inbound.rest.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.experimental.UtilityClass;
import org.mtech.ledger.adapter.inbound.rest.account.api.AccountResponse;
import org.springframework.http.ProblemDetail;

/** OpenAPI documentation for {@link AccountController}. */
@UtilityClass
class Swagger {

    private static final String CREATE_ACCOUNT_REQUEST_EXAMPLE =
            """
            {
              "name": "Jane",
              "surname": "Doe"
            }
            """;

    private static final String NEW_ACCOUNT_EXAMPLE =
            """
            {
              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "name": "Jane",
              "surname": "Doe",
              "balance": 0.00
            }
            """;

    private static final String BALANCE_EXAMPLE =
            """
            {
              "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "name": "Jane",
              "surname": "Doe",
              "balance": 100.00
            }
            """;

    private static final String VALIDATION_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "name must not be blank",
              "instance": "/accounts"
            }
            """;

    private static final String NOT_FOUND_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Account not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "instance": "/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6/balance"
            }
            """;

    static class CreateAccount {

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Operation(
                summary = "Create a new account",
                requestBody =
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                content =
                                        @Content(
                                                examples =
                                                        @ExampleObject(
                                                                value = CREATE_ACCOUNT_REQUEST_EXAMPLE))))
        @ApiResponse(
                responseCode = "201",
                description = "Account successfully created",
                content =
                        @Content(
                                schema = @Schema(implementation = AccountResponse.class),
                                examples = @ExampleObject(value = NEW_ACCOUNT_EXAMPLE)))
        @ApiResponse(
                responseCode = "400",
                description = "Invalid request — blank or oversized name or surname",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = VALIDATION_ERROR_EXAMPLE)))
        @interface Description {}
    }

    static class GetBalance {

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Operation(summary = "Get the current balance of an account")
        @ApiResponse(
                responseCode = "200",
                description = "Current account balance",
                content =
                        @Content(
                                schema = @Schema(implementation = AccountResponse.class),
                                examples = @ExampleObject(value = BALANCE_EXAMPLE)))
        @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = NOT_FOUND_ERROR_EXAMPLE)))
        @interface Description {}
    }
}
