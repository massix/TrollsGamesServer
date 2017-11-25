package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableGamesRepository extends JpaRepository<TableGame, TableGame> {
}
