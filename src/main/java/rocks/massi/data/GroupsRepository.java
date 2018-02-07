package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupsRepository extends JpaRepository<Group, Long> {
    List<Group> findByName(String name);

    List<Group> findByStatus(Group.GroupStatus status);
}
