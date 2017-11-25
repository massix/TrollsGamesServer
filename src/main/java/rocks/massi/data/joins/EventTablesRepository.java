package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventTablesRepository extends JpaRepository<EventTable, EventTable> {
    List<EventTable> findByEventId(int eventId);
}
