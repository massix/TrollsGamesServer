create table if not exists games (
    id int primary key not null,
    name text not null, description text,
    minplayers int,
    maxplayers int,
    playingtime int,
    yearpublished int,
    rank int,
    extension bool,
    thumbnail text,
    authors text,
    expands text
);

create table if not exists users (
    bggNick text primary key not null unique,
    forumNick text not null unique,
    games text not null,
    wants text not null
);

