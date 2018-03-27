package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import rocks.massi.authentication.AuthenticationType;

import javax.transaction.Transactional;
import java.util.ArrayList;

@org.springframework.stereotype.Repository
public interface UsersRepository extends JpaRepository<User, String> {
    ArrayList<User> findByAuthenticationTypeNot(AuthenticationType authenticationType);
    User findByBggNick(final String bggNick);
    User findByForumNick(final String forumNick);

    User findByEmail(final String email);

    @Transactional
    void deleteByBggNick(final String bggNick);
}
