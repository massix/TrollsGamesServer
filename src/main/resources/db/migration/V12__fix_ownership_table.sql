drop table ownerships;

-- Relation between user and game(s)
create table ownerships (
    userid text not null,
    gameid int not null,
    game_name text not null,
    primary key (userid, gameid),
    foreign key(userid) references users(bggNick),
    foreign key(gameid) references games(id)
);
