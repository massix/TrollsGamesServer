package rocks.massi.data.joins;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface OwnershipsRepository extends Repository<Ownership, Integer> {
    Ownership save(final Ownership ownership);
    Ownership delete(final Ownership ownership);

    void deleteAll();
    List<Ownership> findByUser(final String user);

    List<Ownership> findByGame(final int game);
    List<Ownership> findAll();
}
