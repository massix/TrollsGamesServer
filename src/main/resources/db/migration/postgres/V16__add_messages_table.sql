create table messages (
    id serial primary key not null,
    message text not null,
    author text not null,
    group_id bigint default null,
    event_id bigint default null,
    game_id bigint default null,
    message_id bigint default null,
    rating integer default 0,
    date_time timestamp not null
);
