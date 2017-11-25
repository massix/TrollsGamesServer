-- Table to handle single events
create table events (
    id smallint primary key not null,
    name text not null,
    start timestamp not null,
    end timestamp not null
);

-- Table to handle single tables
create table tables (
    id smallint primary key not null,
    name text not null,
    min_players int not null,
    max_players int not null
);

-- Join event -> table
create table event_table (
    table_id smallint not null,
    event_id smallint not null,
    primary key(table_id, event_id)
);

-- Join table -> game
create table table_game (
    table_id smallint not null,
    game_id smallint not null,
    primary key(table_id, game_id)
);

-- Join table -> users
create table table_user (
    table_id smallint not null,
    user_id text not null,
    primary key(table_id, user_id)
);
