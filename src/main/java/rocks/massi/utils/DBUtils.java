package rocks.massi.utils;

import lombok.val;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.User;

public class DBUtils {
    public static User getUser(final DatabaseConnector connector, final String nick) {
        val u = connector.userSelector.findByBggNick(nick);
        if (u == null)
            return connector.userSelector.findByForumNick(nick);
        return u;
    }
}
