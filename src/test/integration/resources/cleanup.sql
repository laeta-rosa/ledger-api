-- Wipes all data before each integration test so the shared in-memory H2
-- database starts empty and tests stay isolated from one another.
-- Referential integrity is toggled off so the tables can be truncated
-- regardless of the account -> account_transaction foreign key.
SET REFERENTIAL_INTEGRITY FALSE;
TRUNCATE TABLE account_transaction;
TRUNCATE TABLE account;
SET REFERENTIAL_INTEGRITY TRUE;