# Tiny Ledger API

A small REST API powering a simple ledger: record deposits and withdrawals, view the current balance, and view transaction history.

Built with **Java 21** and **Spring Boot 4**, persisting to an **in-memory H2 database** via **Spring Data JDBC** — no database server or other software to install.

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
- `409` — the account was modified concurrently (optimistic-lock conflict); retry the request

### Inspecting the database

The H2 web console is enabled at `http://localhost:8080/h2-console` while the app is running. Connect with JDBC URL `jdbc:h2:mem:ledger`, user `sa`, no password, to browse the `account` and `account_transaction` tables.

## Design

Standard three-layer Spring Boot service, kept deliberately small:

- `api/` — REST controllers (`AccountController` for account lifecycle + balance, `TransactionController` for money movements + history), request/response records, and an exception handler that maps domain errors to problem-detail responses. Persistence types are never returned directly; the controllers map them to `AccountResponse` / `TransactionResponse` DTOs.
- `service/` — split by resource, mirroring the controllers: `AccountService` (open accounts, read balance, the shared account lookup) and `TransactionService` (record movements, read history). `TransactionService.record(...)` is `@Transactional`, so the balance update and the appended transaction commit together; it builds on `AccountService` to load and persist the account. Amount validation (positive, at most 2 decimal places) lives in `TransactionService`.
- `repository/` — Spring Data JDBC `CrudRepository` interfaces: `AccountRepository` and `TransactionRepository` (with a derived `findByAccountIdOrderByTimestampDesc` query).
- `domain/` — `Account` (the aggregate root; the no-overdraft invariant lives in `Account.withdraw`), the immutable `Transaction` record (a separate, append-only aggregate referencing its account by id), and domain exceptions.

Data access uses **Spring Data JDBC** rather than JPA/Hibernate: no lazy loading or persistence context to reason about, and the immutable `Transaction` stays a plain record. The schema is defined in `src/main/resources/schema.sql` and applied to the in-memory H2 database at startup.

**Concurrency.** `Account` carries an optimistic-lock `@Version`. Two concurrent movements on the same account can't both commit against a stale balance — the loser gets an `OptimisticLockingFailureException` (surfaced as `409`), so an overdraft can never slip through a race even though the assignment doesn't require atomic operations.

## Assumptions

- **Single currency.** Amounts are decimal numbers with at most 2 decimal places (e.g. `10.50`); no currency field.
- **No overdrafts.** A withdrawal larger than the current balance is rejected with `422`.
- **Multiple accounts** are supported (`POST /accounts`); the spec only asks for one ledger, but scoping by account id keeps the API realistic at little extra cost.
- **In-memory storage.** Data lives in an in-memory H2 database and is lost when the application stops. Swapping to a persistent database is a one-line change to the JDBC URL (plus running the schema against it) — no code changes.
- **Concurrent movements** on the same account are guarded by optimistic locking; a conflicting request receives `409` and can be retried.
- Out of scope, per the assignment: authentication/authorisation, logging/monitoring, pagination, and idempotency keys (natural next steps if this grew up).
