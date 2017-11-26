package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface TableGamesRepository extends JpaRepository<TableGame, Integer> {
    List<TableGame> findByTableId(int tableId);

    @Transactional
    void deleteByTableId(int tableId);
}
