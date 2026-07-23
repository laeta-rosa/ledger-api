# Tiny Ledger API

A small REST API powering a simple ledger: open accounts, record deposits and withdrawals, view the current balance, and view transaction history.

Built with **Java 25** and **Spring Boot 4**, persisting to an **in-memory H2 database** via **Spring Data JDBC** — no database server or other software to install.

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

### Interactive API docs

Swagger UI is served while the app is running:

- **Swagger UI** — `http://localhost:8080/swagger-ui.html`
- **OpenAPI spec** — `http://localhost:8080/v3/api-docs`

Every endpoint carries request/response examples and the full set of error statuses.

## API

| Method | Path | Description |
|---|---|---|
| `POST` | `/accounts` | Open a new account (starts with a 0.00 balance) |
| `GET` | `/accounts/{id}/balance` | Current balance |
| `POST` | `/accounts/{id}/deposit` | Record a deposit |
| `POST` | `/accounts/{id}/withdrawal` | Record a withdrawal |
| `GET` | `/accounts/{id}/transactions` | Transaction history, newest first |

### Examples

Open an account (name and surname are required):

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
# {"id":"...","name":"Jane","surname":"Doe","balance":70.00}
```

View transaction history (newest first):

```bash
curl -s localhost:8080/accounts/<id>/transactions
```

### Errors

Errors are returned as RFC 9457 `application/problem+json` bodies:

- `404` — unknown account
- `400` — invalid request (blank/oversized name or surname; missing/negative/zero amount; more than 2 decimal places; more than 12 integer digits; malformed body)
- `422` — withdrawal exceeding the current balance
- `409` — the account was modified concurrently (optimistic-lock conflict); retry the request

### Inspecting the database

The H2 web console is enabled at `http://localhost:8080/h2-console` while the app is running. Connect with JDBC URL `jdbc:h2:mem:ledger`, user `sa`, no password, to browse the `account` (id, name, surname, balance, version) and `account_transaction` tables.

## Design

Standard three-layer Spring Boot service, kept deliberately small and organised by feature. The `api` and `domain` layers are each split into `account` and `transaction` sub-packages so the two resources stay self-contained.

- `api/` — REST controllers, request/response records, and a global exception handler that maps domain errors to problem-detail responses. `api/account` holds `AccountController` (account lifecycle + balance); `api/transaction` holds `TransactionController` (money movements + history) plus a `validator` sub-package with the bean-validation `@ValidAmount` constraint and its `AmountValidator`. Each sub-package carries a `Swagger` class holding the OpenAPI annotations, keeping the controllers readable. Persistence types are never returned directly; the controllers map them to `AccountResponse` / `TransactionResponse` DTOs.
- `service/` — split by resource, mirroring the controllers: `AccountService` (open accounts, read balance, the shared account lookup and save) and `TransactionService` (record movements, read history). `TransactionService.record(...)` is `@Transactional`, so the balance update and the appended transaction commit together; it builds on `AccountService` to load and persist the account.
- `repository/` — Spring Data JDBC `CrudRepository` interfaces: `AccountRepository` and `TransactionRepository` (with a derived `findByAccountIdOrderByTimestampDesc` query).
- `domain/` — `domain/account` holds `Account` (the aggregate root; the no-overdraft invariant lives in `Account.withdraw`) and the domain exceptions. `domain/transaction` holds the immutable `Transaction` record (a separate, append-only aggregate referencing its account by id) and `TransactionType` (`DEPOSIT` / `WITHDRAWAL`), which dispatches to the matching `Account` method through a named `BalanceOperation` functional interface.

**Amount validation.** Monetary amounts are validated declaratively at the API boundary with the `@ValidAmount` constraint on `CreateTransactionRequest`: strictly positive, at most 2 significant decimal places (trailing zeros don't count, so `5.500` is accepted as `5.50`), and at most 12 integer digits. The service normalises the accepted amount to scale 2 before recording it.

Data access uses **Spring Data JDBC** rather than JPA/Hibernate: no lazy loading or persistence context to reason about, and the immutable `Transaction` stays a plain record. The schema is defined in `src/main/resources/schema.sql` and applied to the in-memory H2 database at startup.

**Concurrency.** `Account` carries an optimistic-lock `@Version`. Two concurrent movements on the same account can't both commit against a stale balance — the loser gets an `OptimisticLockingFailureException` (surfaced as `409`), so an overdraft can never slip through a race even though the assignment doesn't require atomic operations.

**Testing.** No mocks. A shared `@IntegrationTest` meta-annotation (in `meta/`) boots the full application on a random port, enables constructor injection, and truncates every table before each test via `cleanup.sql`, so tests stay isolated against the shared in-memory H2 database. The service tests (`AccountServiceTest`, `TransactionServiceTest`) run against the real `@Service` beans and that database; `TransactionTypeTest` covers the deposit/withdrawal dispatch in the domain, and `AmountValidatorTest` the amount constraint. The API tests exercise the app over real HTTP with **REST Assured** — actual requests through the servlet stack, JSON serialization, and status codes, not a mocked dispatcher — mirroring the controller split: `AccountControllerTest` (creation, balance) and `TransactionControllerTest` (recording movements, history, error statuses), sharing setup through `AbstractControllerTest`. Two small support layers keep them readable: `harness/` holds `RestTestHarness` (issues the HTTP calls) and `DatabaseTestHarness` (reads persisted rows back to assert what actually landed, independent of the HTTP responses), while `fixture/` holds reusable request bodies (`AccountRequest`, `TransactionRequest`) behind a `TestMessage` interface.

## Assumptions

- **Named accounts.** Opening an account requires a non-blank `name` and `surname` (each at most 100 characters); there is no auth, so this is descriptive only.
- **Single currency.** Amounts are decimal numbers with at most 2 decimal places (e.g. `10.50`); no currency field.
- **No overdrafts.** A withdrawal larger than the current balance is rejected with `422`.
- **Multiple accounts** are supported (`POST /accounts`); the spec only asks for one ledger, but scoping by account id keeps the API realistic at little extra cost.
- **In-memory storage.** Data lives in an in-memory H2 database and is lost when the application stops. Swapping to a persistent database is a one-line change to the JDBC URL (plus running the schema against it) — no code changes.
- **Concurrent movements** on the same account are guarded by optimistic locking; a conflicting request receives `409` and can be retried.
- Out of scope, per the assignment: authentication/authorisation, logging/monitoring, pagination, and idempotency keys (natural next steps if this grew up).
