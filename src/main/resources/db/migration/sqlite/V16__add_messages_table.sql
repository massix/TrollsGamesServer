create table messages (
    id integer primary key not null,
    message text not null,
    author text not null,
    group_id integer default null,
    event_id integer default null,
    game_id integer default null,
    message_id integer default null,
    rating integer default 0,
    date_time timestamp not null
);
