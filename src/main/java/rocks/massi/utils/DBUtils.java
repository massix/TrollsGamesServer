package rocks.massi.utils;

import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;

public class DBUtils {
    public static User getUser(final UsersRepository usersRepository, final String nick) {
        User u = usersRepository.findByBggNick(nick);

        if (u == null)
            return usersRepository.findByForumNick(nick);

        return u;
    }
}
