package rocks.massi.data;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface UsersRepository extends Repository<User, String> {
    User save(final User user);
    User findByBggNick(final String bggNick);
    User findByForumNick(final String forumNick);
    List<User> findAll();
    User delete(final User user);
    User deleteByBggNick(final String bggNick);
    void deleteAll();
}
