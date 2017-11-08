-- Relation between user and game(s)
create table ownerships (
    id integer primary key autoincrement,
    user text,
    game int,
    unique (user, game) on conflict replace,
    foreign key(user) references users(bggNick),
    foreign key(game) references games(id)
);

-- Honors
create table honors (
    id integer primary key,
    description text
);

-- Relation between game and honors
create table game_honors (
    id integer primary key autoincrement,
    game int,
    honor int,
    unique (game, honor) on conflict replace,
    foreign key(game) references games(id),
    foreign key(honor) references honors(id)
);

-- Migrate users to new schema
alter table users rename to users_old;
create table users (
    bggNick text primary key not null,
    forumNick text
);

-- Port users
insert into users select bggnick, forumnick from users_old;
drop table users_old;