create table events (
    id smallint primary key not null,
    name text not null,
    start timestamp not null,
    end timestamp not null
);