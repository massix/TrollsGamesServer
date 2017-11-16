package rocks.massi.data.joins;

import org.springframework.data.repository.Repository;

import javax.transaction.Transactional;
import java.util.List;

@org.springframework.stereotype.Repository
public interface OwnershipsRepository extends Repository<Ownership, Integer> {
    Ownership save(final Ownership ownership);
    Ownership delete(final Ownership ownership);

    @Transactional
    void deleteAll();

    @Transactional
    void deleteByUser(final String user);

    @Transactional
    void deleteByGame(final int game);

    List<Ownership> findByUser(final String user);

    List<Ownership> findByGame(final int game);
    List<Ownership> findAll();
}
