package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesRepository extends JpaRepository<Message, Long> {

    @Query("select m from Message m where m.eventId = null and m.groupId = null and m.gameId = null and m.messageId = null order by m.dateTime desc")
    List<Message> findAllForHomepage();

    List<Message> findByGroupIdOrderByDateTimeDesc(Long group);

    List<Message> findByEventIdOrderByDateTimeDesc(Long event);

    List<Message> findByGameIdOrderByDateTimeDesc(Long game);

    List<Message> findByMessageIdOrderByDateTimeDesc(Long message);
}
