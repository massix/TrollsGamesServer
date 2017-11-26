package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface EventTablesRepository extends JpaRepository<EventTable, Integer> {
    List<EventTable> findByEventId(int eventId);

    @Transactional
    void deleteByTableId(int tableId);

    @Transactional
    void deleteByEventId(int eventId);
}
