alter table users add column password text not null default '';
alter table users add column role text not null default 'user';
alter table users add column authentication_type text not null default 'none';