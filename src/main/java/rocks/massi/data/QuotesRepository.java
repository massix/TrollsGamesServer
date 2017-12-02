package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface QuotesRepository extends JpaRepository<Quote, String> {
    Quote findByQuote(String quote);

    @Transactional
    void deleteByQuote(String quote);

}
