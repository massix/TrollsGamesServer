package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventTablesRepository extends JpaRepository<EventTable, EventTable> {
}
