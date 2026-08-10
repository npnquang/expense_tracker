DROP TABLE IF EXISTS expense CASCADE;

DROP TABLE IF EXISTS user_info CASCADE;

DROP TYPE IF EXISTS expense_type;

CREATE TYPE expense_type AS ENUM(
    'FOOD',
    'TRANSPORT',
    'HOUSING',
    'UTILITIES',
    'OTHER'
);

CREATE TYPE user_role_enum AS ENUM(
    'ADMIN',
    'USER'
);

CREATE TABLE user_info (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    user_role user_role_enum NOT NULL DEFAULT 'USER',
    CONSTRAINT unique_username UNIQUE (username),
    CONSTRAINT unique_email UNIQUE (email)
);

CREATE TABLE expense (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    description VARCHAR(255) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    type expense_type NOT NULL,
    created_at timestamptz NOT NULL DEFAULT NOW(),
    user_id BIGINT NOT NULL,
    CONSTRAINT expense_user_fk FOREIGN Key (user_id) REFERENCES user_info (id)
);

CREATE INDEX expense_user_time ON expense (
    user_id,
    created_at
);