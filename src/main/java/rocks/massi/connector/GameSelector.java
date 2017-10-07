package rocks.massi.connector;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
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
                    resultSet.getString("expands"),
                    resultSet.getString("thumbnail"),
                    resultSet.getString("authors"));
        }
    }

    @SqlQuery("select * from games where id = :id")
    Game findById(@Bind("id") final int id);

    @SqlQuery("select * from games")
    List<Game> getGames();
}