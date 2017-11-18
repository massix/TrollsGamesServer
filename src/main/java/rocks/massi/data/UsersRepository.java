package rocks.massi.data;

import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

@org.springframework.stereotype.Repository
public interface UsersRepository extends Repository<User, String> {
    User save(final User user);
    User findByBggNick(final String bggNick);
    User findByForumNick(final String forumNick);

    User findByEmail(final String email);
    List<User> findAll();

    @Transactional
    User delete(final User user);

    @Transactional
    void deleteByBggNick(final String bggNick);

    @Transactional
    void deleteAll();
}
