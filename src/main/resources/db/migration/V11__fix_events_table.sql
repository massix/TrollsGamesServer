-- Drop old tables, recreate them using bigints
drop table table_user;
drop table table_game;
drop table event_table;
drop table events;
drop table tables;

-- Table to handle single events
create table events (
    id integer primary key not null,
    name text not null,
    start_date timestamp not null,
    end_date timestamp not null
);

-- Table to handle single tables
create table tables (
    id integer primary key not null,
    name text not null,
    min_players int not null,
    max_players int not null
);

-- Join event -> table
create table event_table (
    table_id integer not null,
    event_id integer not null,
    primary key(table_id, event_id)
);

-- Join table -> game
create table table_game (
    table_id integer not null,
    game_id bigint not null,
    primary key(table_id, game_id)
);

-- Join table -> users
create table table_user (
    table_id integer not null,
    user_id text not null,
    primary key(table_id, user_id)
);
