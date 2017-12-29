package rocks.massi.data.joins;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UsersGroupsRepository extends JpaRepository<UsersGroups, UsersGroups.UsersGroupsKey> {
    List<UsersGroups> findByUserId(String userId);

    List<UsersGroups> findByGroupId(Long groupId);

    @Transactional
    void deleteByGroupId(Long groupId);
}
