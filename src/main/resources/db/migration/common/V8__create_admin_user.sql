delete from ownerships where userid = 'massi_x';
delete from users where bggNick in (select bggNick from users where bggNick = 'massi_x');
insert into users (
    bggnick,
    forumnick,
    password,
    role,
    authentication_type,
    email
) values (
    'massi_x',
    'Massi',
    '$2a$10$o3w6Dt0sYk2mGjK4LF5m9eAMKbpjU.kby7kdKI.y/Gf8vBYheLIgG',
    'ADMIN',
    'JWT',
    'massi@massi.rocks'
);
