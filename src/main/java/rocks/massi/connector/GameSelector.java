package rocks.massi.connector;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import rocks.massi.data.Game;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


@RegisterMapper(GameSelector.GameMapper.class)
public interface GameSelector {

    class GameMapper implements ResultSetMapper<Game> {
        @Override
        public Game map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new Game(
                    resultSet.getInt("id"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getInt("minplayers"),
                    resultSet.getInt("maxplayers"),
                    resultSet.getInt("playingtime"),
                    resultSet.getInt("yearpublished"),
                    resultSet.getInt("rank"),
                    resultSet.getBoolean("extension"),
                    resultSet.getString("thumbnail"),
                    resultSet.getString("authors"),
                    resultSet.getString("expands"));
        }
    }

    @SqlQuery("select * from games where id = :id")
    Game findById(@Bind("id") final int id);

    @SqlQuery("select * from games")
    List<Game> getGames();

    @SqlUpdate("insert into games values (:id, :name, :description, :minPlayers, :maxPlayers, " +
            ":playingTime, :yearPublished, :rank, :extension, :expands, :thumbnail, :authors)")
    void insertGame(@BindBean Game g);

    @SqlUpdate("delete from games where id = :id")
    void removeGame(@BindBean Game g);

    @SqlUpdate("delete from games where id = :id")
    void removeGameById(@Bind("id") final int id);
}
