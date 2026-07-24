package org.mtech.ledger.adapter.inbound.rest.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import lombok.experimental.UtilityClass;
import org.springframework.http.ProblemDetail;

/** OpenAPI documentation for {@link TransactionController}. */
@UtilityClass
class Swagger {

    private static final String DEPOSIT_REQUEST_EXAMPLE =
            """
            {
              "amount": 100.00
            }
            """;

    private static final String WITHDRAWAL_REQUEST_EXAMPLE =
            """
            {
              "amount": 25.00
            }
            """;

    private static final String DEPOSIT_EXAMPLE =
            """
            {
              "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
              "type": "DEPOSIT",
              "amount": 100.00,
              "timestamp": "2026-07-20T19:42:04.687Z",
              "balanceAfter": 100.00
            }
            """;

    private static final String WITHDRAWAL_EXAMPLE =
            """
            {
              "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
              "type": "WITHDRAWAL",
              "amount": 25.00,
              "timestamp": "2026-07-20T19:43:12.114Z",
              "balanceAfter": 75.00
            }
            """;

    private static final String HISTORY_EXAMPLE =
            """
            [
              {
                "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                "type": "WITHDRAWAL",
                "amount": 25.00,
                "timestamp": "2026-07-20T19:43:12.114Z",
                "balanceAfter": 75.00
              },
              {
                "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                "type": "DEPOSIT",
                "amount": 100.00,
                "timestamp": "2026-07-20T19:42:04.687Z",
                "balanceAfter": 100.00
              }
            ]
            """;

    private static final String AMOUNT_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Bad Request",
              "status": 400,
              "detail": "amount must be a positive number",
              "instance": "/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6/deposit"
            }
            """;

    private static final String NOT_FOUND_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Not Found",
              "status": 404,
              "detail": "Account not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
              "instance": "/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6/transactions"
            }
            """;

    private static final String INSUFFICIENT_FUNDS_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Unprocessable Content",
              "status": 422,
              "detail": "Insufficient funds: balance is 75.00, requested withdrawal of 100.00",
              "instance": "/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6/withdrawal"
            }
            """;

    private static final String CONFLICT_ERROR_EXAMPLE =
            """
            {
              "type": "about:blank",
              "title": "Conflict",
              "status": 409,
              "detail": "The account was modified concurrently. Please retry.",
              "instance": "/accounts/3fa85f64-5717-4562-b3fc-2c963f66afa6/deposit"
            }
            """;

    static class GetHistory {

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Operation(summary = "List the transaction history for an account")
        @ApiResponse(
                responseCode = "200",
                description = "Transaction history, most recent first",
                content =
                        @Content(
                                array =
                                        @ArraySchema(
                                                schema = @Schema(implementation = TransactionResponse.class)),
                                examples = @ExampleObject(value = HISTORY_EXAMPLE)))
        @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = NOT_FOUND_ERROR_EXAMPLE)))
        @interface Description {}
    }

    static class Deposit {

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Operation(
                summary = "Deposit funds into an account",
                requestBody =
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                content =
                                        @Content(
                                                examples =
                                                        @ExampleObject(value = DEPOSIT_REQUEST_EXAMPLE))))
        @ApiResponse(
                responseCode = "201",
                description = "Deposit successfully recorded",
                content =
                        @Content(
                                schema = @Schema(implementation = TransactionResponse.class),
                                examples = @ExampleObject(value = DEPOSIT_EXAMPLE)))
        @ApiResponse(
                responseCode = "400",
                description = "Invalid amount",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = AMOUNT_ERROR_EXAMPLE)))
        @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = NOT_FOUND_ERROR_EXAMPLE)))
        @ApiResponse(
                responseCode = "409",
                description = "The account was modified concurrently — retry",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = CONFLICT_ERROR_EXAMPLE)))
        @interface Description {}
    }

    static class Withdraw {

        @Target(ElementType.METHOD)
        @Retention(RetentionPolicy.RUNTIME)
        @Operation(
                summary = "Withdraw funds from an account",
                requestBody =
                        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                content =
                                        @Content(
                                                examples =
                                                        @ExampleObject(value = WITHDRAWAL_REQUEST_EXAMPLE))))
        @ApiResponse(
                responseCode = "201",
                description = "Withdrawal successfully recorded",
                content =
                        @Content(
                                schema = @Schema(implementation = TransactionResponse.class),
                                examples = @ExampleObject(value = WITHDRAWAL_EXAMPLE)))
        @ApiResponse(
                responseCode = "400",
                description = "Invalid amount",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = AMOUNT_ERROR_EXAMPLE)))
        @ApiResponse(
                responseCode = "404",
                description = "Account not found",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = NOT_FOUND_ERROR_EXAMPLE)))
        @ApiResponse(
                responseCode = "422",
                description = "Insufficient funds for the requested withdrawal",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = INSUFFICIENT_FUNDS_ERROR_EXAMPLE)))
        @ApiResponse(
                responseCode = "409",
                description = "The account was modified concurrently — retry",
                content =
                        @Content(
                                schema = @Schema(implementation = ProblemDetail.class),
                                examples = @ExampleObject(value = CONFLICT_ERROR_EXAMPLE)))
        @interface Description {}
    }
}
