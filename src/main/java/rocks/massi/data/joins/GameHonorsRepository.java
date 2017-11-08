package rocks.massi.data.joins;

import org.springframework.data.repository.Repository;

import java.util.List;

@org.springframework.stereotype.Repository
public interface GameHonorsRepository extends Repository<GameHonor, Integer> {
    GameHonor save(final GameHonor gameHonor);

    void deleteAll();

    List<GameHonor> findByGame(final int game);

    List<GameHonor> findByHonor(final int honor);
}
