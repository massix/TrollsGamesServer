package rocks.massi.connector;

import org.skife.jdbi.v2.sqlobject.SqlUpdate;

public interface BaseSelector {

    @SqlUpdate("create table if not exists games " +
            "(id int primary key not null, name text not null, description text, minplayers int," +
            "maxplayers int, playingtime int, yearpublished int, rank int, extension bool, thumbnail text," +
            "authors text, expands int)")
    void createTableGames();

    @SqlUpdate("create table if not exists users (bggnick text primary key not null, forumnick not null, games text, wants text)")
    void createTableUsers();
}
