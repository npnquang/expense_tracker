DROP TABLE IF EXISTS expense CASCADE;
DROP TYPE IF EXISTS expense_type;

CREATE TYPE expense_type AS ENUM (
    'FOOD',
    'TRANSPORT',
    'HOUSING',
    'UTILITIES',
    'OTHER'
    );

CREATE TABLE expense
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    description VARCHAR(255)   NOT NULL,
    amount      NUMERIC(10, 2) NOT NULL,
    type        expense_type   NOT NULL,
    created_at  timestamptz    NOT NULL DEFAULT NOW()
);