create table tokens (
    token_type text not null,
    user_email text unique not null,
    token_value text primary key not null,
    token_key text not null
);
