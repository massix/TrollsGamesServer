package rocks.massi.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokensRepository extends JpaRepository<Token, String> {
    Token findByTokenValue(String tokenValue);

    Token findByUserEmail(String userEmail);
}
