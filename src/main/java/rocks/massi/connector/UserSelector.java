package rocks.massi.connector;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import rocks.massi.data.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterMapper(UserSelector.UserMapper.class)
public interface UserSelector {

    class UserMapper implements ResultSetMapper<User> {

        @Override
        public User map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new User(
                    resultSet.getString("bggNick"),
                    resultSet.getString("forumNick"),
                    resultSet.getString("games"),
                    resultSet.getString("wants")
            );
        }
    }

    @SqlQuery("select * from users where bggnick = :nick")
    User findByBggNick(@Bind("nick") final String nick);

    @SqlQuery("select * from users where forumnick = :nick")
    User findByForumNick(@Bind("nick") final String nick);

    @SqlQuery("select * from users")
    List<User> getUsers();

    @SqlUpdate("insert into users values (:bggNick, :forumNick, :games, :wants)")
    void addUser(@BindBean final User user);

    @SqlUpdate("update users set games = :games, wants = :wants where bggNick = :bggNick")
    void updateCollectionForUser(@BindBean final User user);
}
