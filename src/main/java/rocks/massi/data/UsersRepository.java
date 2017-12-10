package rocks.massi.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@org.springframework.stereotype.Repository
public interface UsersRepository extends JpaRepository<User, String> {
    User findByBggNick(final String bggNick);
    User findByForumNick(final String forumNick);

    User findByEmail(final String email);

    Page<Game> findCollectionByBggNick(String bggNick, Pageable pageable);

    @Transactional
    void deleteByBggNick(final String bggNick);
}
