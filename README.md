# Tiny Ledger API

A small REST API powering a simple ledger: record deposits and withdrawals, view the current balance, and view transaction history.

Built with **Java 21** and **Spring Boot 4**, storing everything in memory — no database or other software to install.

## Requirements

- JDK 21+ (nothing else — the Gradle wrapper downloads everything)

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
| `POST` | `/accounts/{id}/transactions` | Record a deposit or withdrawal |
| `GET` | `/accounts/{id}/transactions` | Transaction history, newest first |

### Examples

Create an account:

```bash
curl -s -X POST localhost:8080/accounts
# {"id":"6f1f9273-...","balance":0.00}
```

Deposit 100.00 (use the `id` returned above):

```bash
curl -s -X POST localhost:8080/accounts/<id>/transactions \
  -H 'Content-Type: application/json' \
  -d '{"type": "DEPOSIT", "amount": 100.00}'
# {"id":"...","type":"DEPOSIT","amount":100.00,"timestamp":"...","balanceAfter":100.00}
```

Withdraw 30.00:

```bash
curl -s -X POST localhost:8080/accounts/<id>/transactions \
  -H 'Content-Type: application/json' \
  -d '{"type": "WITHDRAWAL", "amount": 30.00}'
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
- `400` — invalid request (missing/negative/zero amount, more than 2 decimal places, unknown transaction type, malformed body)
- `422` — withdrawal exceeding the current balance

## Design

Standard three-layer Spring Boot service, kept deliberately small:

- `api/` — REST controller, request/response records, and an exception handler that maps domain errors to problem-detail responses.
- `service/` — `LedgerService` holds the business rules: amounts must be positive with at most 2 decimal places, and withdrawals cannot overdraw the account. Each recorded transaction stores the resulting balance (`balanceAfter`), so history doubles as an audit trail.
- `repository/` — `InMemoryAccountRepository`, a `ConcurrentHashMap` of accounts.
- `domain/` — `Account`, immutable `Transaction` record, and domain exceptions.

## Assumptions

- **Single currency.** Amounts are decimal numbers with at most 2 decimal places (e.g. `10.50`); no currency field.
- **No overdrafts.** A withdrawal larger than the current balance is rejected with `422`.
- **Multiple accounts** are supported (`POST /accounts`); the spec only asks for one ledger, but scoping by account id keeps the API realistic at little extra cost.
- **In-memory storage.** All data is lost when the application stops.
- **Thread safety** for balance updates is handled with a per-account lock so the balance and history stay consistent under concurrent requests, even though the spec does not require atomic operations.
- Out of scope, per the assignment: authentication/authorisation, logging/monitoring, persistence, pagination, and idempotency keys (natural next steps if this grew up).
