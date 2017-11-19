package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuotesRepository extends JpaRepository<Quote, String> {
}
