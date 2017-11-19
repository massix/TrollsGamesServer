package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stats, Stats.StatsKey> {
    List<Stats> findAllByOrderByHashedUser();

}
