CREATE TABLE IF NOT EXISTS account (
    id      UUID PRIMARY KEY,
    balance DECIMAL(19, 2) NOT NULL,
    version BIGINT
);

CREATE TABLE IF NOT EXISTS account_transaction (
    id            UUID PRIMARY KEY,
    account_id    UUID           NOT NULL REFERENCES account (id),
    type          VARCHAR(20)    NOT NULL,
    amount        DECIMAL(19, 2) NOT NULL,
    timestamp     TIMESTAMP      NOT NULL,
    balance_after DECIMAL(19, 2) NOT NULL
);
