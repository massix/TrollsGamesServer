-- Migrate users to new schema
alter table users rename to users_old;
create table users (
    bggNick text primary key not null,
    forumNick text
);

-- Port users
insert into users select bggnick, forumnick from users_old;
drop table users_old;

-- Relation between user and game(s)
create table ownerships (
    userid text not null,
    gameid int not null,
    primary key (userid, gameid),
    foreign key(userid) references users(bggNick),
    foreign key(gameid) references games(id)
);

-- Honors
create table honors (
    id integer primary key,
    description text
);

-- Relation between game and honors
create table game_honors (
    game int not null,
    honor int not null,
    primary key (game, honor),
    foreign key(game) references games(id),
    foreign key(honor) references honors(id)
);
