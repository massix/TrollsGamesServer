package rocks.massi.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface GamesRepository extends Repository<Game, Integer> {
    Game save(final Game game);
    Game findById(final int id);
    List<Game> findAll();
    Page<Game> findAll(Pageable page);
    Page<Game> findAllByOrderByNameAsc(Pageable page);

    List<Game> findByNameContainingIgnoreCase(final String name);
    Game delete(final Game game);
    void deleteAll();
}
