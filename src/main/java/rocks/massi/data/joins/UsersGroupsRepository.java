package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsersGroupsRepository extends JpaRepository<UsersGroups, UsersGroups.UsersGroupsKey> {
    List<UsersGroups> findByUserId(String userId);
}
