create table ownerships (
    id integer primary key autoincrement,
    user text,
    game int,
    unique (user, game) on conflict replace,
    foreign key(user) references users(bggNick),
    foreign key(game) references games(id)
);

alter table users rename to users_old;

create table users (
    bggNick text primary key not null,
    forumNick text
);

insert into users select bggnick, forumnick from users_old;
drop table users_old;