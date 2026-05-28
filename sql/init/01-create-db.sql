drop table if exists expense cascade;
drop type if exists expense_type;

create type expense_type as enum (
    'FOOD',
    'TRANSPORT',
    'HOUSING',
    'UTILITIES',
    'OTHER'
);

create table expense
(
    id BIGINT primary key generated always as identity,
    description varchar(255)   not null,
    amount      numeric(10, 2) not null,
    type        expense_type   not null,
    created_at  date           not null
);