create table stats (
    hashed_user text not null,
    endpoint text not null,
    counter int not null,
    primary key(hashed_user, endpoint)
);
