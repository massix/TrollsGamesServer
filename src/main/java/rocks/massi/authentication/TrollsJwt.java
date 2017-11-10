package rocks.massi.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import javafx.util.Pair;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import rocks.massi.data.User;

import java.util.*;

@Slf4j
@Data
@Component
public class TrollsJwt {

    public enum KeyRetrievalMethod {
        DEV,
        RANDOM,
        DATABASE
    }

    public static String ROLE_KEY = "role";
    public static String USER_KEY = "user";

    private List<String> keys;

    // username -> (token key)
    private Map<String, Pair<String, String>> tokens;

    @Bean
    @Primary
    public static TrollsJwt makeTokenCreator(@Value("${token.key.retrieval}") final String retrieval,
                                             @Value("${token.key.iterations}") final Integer iterations) throws Exception {
        TrollsJwt token = new TrollsJwt();
        token.keys = new LinkedList<>();
        token.tokens = new HashMap<>();
        log.info("Create new TrollsJwt using method {} with {} iterations", retrieval, iterations);

        for (int i = 0; i < iterations; i++) {
            switch (KeyRetrievalMethod.valueOf(retrieval)) {
                case DEV:
                    token.keys.add("test");
                    break;
                case DATABASE:
                    // load keys from db
                    break;
                default:
                    // generate random key
                    token.keys.add(UUID.randomUUID().toString());
                    break;
            }
        }

        return token;
    }

    public boolean checkTokenForUser(final User user) {
        if (!tokens.containsKey(user.getBggNick())) {
            log.error("I have no token in here for user {}", user.getBggNick());
            return false;
        }

        Pair<String, String> stringPair = tokens.get(user.getBggNick());
        Claims parsedToken = Jwts.parser().setSigningKey(stringPair.getKey()).parseClaimsJws(stringPair.getValue()).getBody();

        if (!parsedToken.get(USER_KEY).equals(user.getBggNick())) {
            log.error("User mismatch while verifying token for user {}", user.getBggNick());
            return false;
        }

        return true;
    }

    public String generateNewTokenForUser(final User user) {
        // Get a random key to sign our token
        String key = keys.get(new Random().nextInt(keys.size()));
        String generatedToken = Jwts.builder()
                .claim(USER_KEY, user.getBggNick())
                .claim(ROLE_KEY, user.getRole())
                .setExpiration(new Date(System.currentTimeMillis() + 640_000_000L))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        tokens.put(user.getBggNick(), new Pair<>(key, generatedToken));
        return generatedToken;
    }
}
