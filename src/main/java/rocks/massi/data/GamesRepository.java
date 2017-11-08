package rocks.massi.data;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface GamesRepository extends Repository<Game, Integer> {
    Game save(final Game game);
    Game findById(final int id);
    List<Game> findAll();
    Game delete(final Game game);
    void deleteAll();
}
