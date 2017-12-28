create table users_groups (
    user_id text not null,
    group_id integer not null,
    role text not null,
    primary key(user_id, group_id)
);
