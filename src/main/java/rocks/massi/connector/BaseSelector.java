package rocks.massi.connector;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface BaseSelector {

    @SqlUpdate("create table if not exists games " +
            "(id int primary key not null, name text not null, description text, minplayers int," +
            "maxplayers int, playingtime int, yearpublished int, rank int, extension bool, thumbnail text," +
            "authors text, expands text)")
    void createTableGames();

    @SqlUpdate("create table if not exists users (bggnick text primary key not null, forumnick text not null, games text, wants text)")
    void createTableUsers();

    @SqlUpdate("drop table users")
    void dropTableUsers();

    @SqlUpdate("drop table games")
    void dropTableGames();

    @SqlUpdate("delete from users")
    void truncateTableUsers();

    @SqlUpdate("delete from games")
    void truncateTableGames();
}
