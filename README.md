# Tiny Ledger API

A small REST API powering a simple ledger: record deposits and withdrawals, view the current balance, and view transaction history.

Built with **Java 25** and **Spring Boot 4**, backed by an in-memory H2 database (via Spring Data JDBC) — nothing else to install, and all data is lost when the application stops.

## Requirements

- JDK 25+ (nothing else — the Gradle wrapper downloads everything)

## Run

```bash
./gradlew bootRun
```

The API starts on `http://localhost:8080`.

Run the tests with:

```bash
./gradlew test
```

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/accounts` | Create a new account (starts with a 0.00 balance) |
| `GET` | `/accounts/{id}/balance` | Current balance |
| `POST` | `/accounts/{id}/deposit` | Record a deposit |
| `POST` | `/accounts/{id}/withdrawal` | Record a withdrawal |
| `GET` | `/accounts/{id}/transactions` | Transaction history, newest first |

Interactive API docs are served at `http://localhost:8080/swagger-ui.html`.

### Examples

Create an account:

```bash
curl -s -X POST localhost:8080/accounts \
  -H 'Content-Type: application/json' \
  -d '{"name": "Jane", "surname": "Doe"}'
# {"id":"6f1f9273-...","name":"Jane","surname":"Doe","balance":0.00}
```

Deposit 100.00 (use the `id` returned above):

```bash
curl -s -X POST localhost:8080/accounts/<id>/deposit \
  -H 'Content-Type: application/json' \
  -d '{"amount": 100.00}'
# {"id":"...","type":"DEPOSIT","amount":100.00,"timestamp":"...","balanceAfter":100.00}
```

Withdraw 30.00:

```bash
curl -s -X POST localhost:8080/accounts/<id>/withdrawal \
  -H 'Content-Type: application/json' \
  -d '{"amount": 30.00}'
```

Check the balance:

```bash
curl -s localhost:8080/accounts/<id>/balance
# {"id":"...","balance":70.00}
```

View transaction history (newest first):

```bash
curl -s localhost:8080/accounts/<id>/transactions
```

### Errors

Errors are returned as RFC 9457 `application/problem+json` bodies:

- `404` — unknown account
- `400` — invalid request (missing/negative/zero amount, more than 2 decimal places, blank/oversized name, malformed body)
- `409` — the account was modified concurrently (optimistic-lock conflict); retry
- `422` — withdrawal exceeding the current balance

## Design

Lightweight hexagonal architecture — the ports-and-adapters shape without the
ceremony. There are no port interfaces and each use case has a single concrete
implementation, so nothing is abstracted "just in case."

- `adapter/inbound/rest/` — REST controllers, request/response records, bean-validation for amounts, and an exception handler mapping domain errors to RFC 9457 problem-detail responses. Controllers translate HTTP into use-case commands/queries and back.
- `application/` — the use cases. Each is a command use case (`CreateAccountUseCase`, `RecordTransactionUseCase`) or a query use case (`GetAccountBalanceUseCase`, `GetTransactionHistoryUseCase`) that takes a `*Command`/`*Query` and returns a `*Result`. Business rules — amounts positive with at most 2 decimal places, withdrawals cannot overdraw — are enforced here and in the domain.
- `adapter/outbound/repository/` — Spring Data JDBC repositories, the outbound adapters. Because Spring Data generates the implementation, they double as the persistence boundary — no separate port is needed.
- `domain/` — `Account` aggregate, immutable `Transaction` record, `TransactionType`, and domain exceptions. Each recorded transaction stores the resulting balance (`balanceAfter`), so history doubles as an audit trail.

Tests mirror this split: `src/test/.../integration/` boots the full application (controllers, use cases, repositories) against H2, while `src/test/.../unit/` covers pure logic (the amount validator, `TransactionType`) with no Spring context.

## Assumptions

- **Single currency.** Amounts are decimal numbers with at most 2 decimal places (e.g. `10.50`); no currency field.
- **No overdrafts.** A withdrawal larger than the current balance is rejected with `422`.
- **Multiple accounts** are supported (`POST /accounts`); the spec only asks for one ledger, but scoping by account id keeps the API realistic at little extra cost.
- **In-memory storage.** An in-memory H2 database holds the data; it is lost when the application stops.
- **Concurrency safety** for balance updates is handled with optimistic locking (a `version` column on `account`), so a lost update from concurrent movements is rejected with `409` rather than silently overdrawing.
- Out of scope, per the assignment: authentication/authorisation, logging/monitoring, durable persistence, pagination, and idempotency keys (natural next steps if this grew up).
