package rocks.massi.connector;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
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
                    resultSet.getString("bggnick"),
                    resultSet.getString("forumnick"),
                    resultSet.getString("games"),
                    resultSet.getString("wants")
            );
        }
    }

    @SqlQuery("select * from users where bggnick = :nick")
    User findByBggNick(@Bind("nick") final String nick);

    @SqlQuery("select * from users")
    List<User> getUsers();
}
