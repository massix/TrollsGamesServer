package rocks.massi.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @Getter
    @RequiredArgsConstructor
    private class KeyTokenPair {
        private final String key;
        private final String token;
    }

    @Getter
    @RequiredArgsConstructor
    private class KeyUsernamePair {
        private final String key;
        private final String username;
    }

    @Getter
    @RequiredArgsConstructor
    private class KeyUserTokenTriple {
        private final String key;
        private final User user;
        private final String token;
    }

    public enum KeyRetrievalMethod {
        DEV,
        RANDOM,
        DATABASE
    }

    public static String ROLE_KEY = "role";
    public static String USER_KEY = "user";
    public static String AUTH_KEY = "authentication";
    public static String EMAIL_KEY = "email";

    private List<String> keys;

    // username -> (key token)
    private Map<String, KeyTokenPair> usernameToKeyTokenMap;

    // token -> (key username)
    private Map<String, KeyUsernamePair> tokenToKeyUsernameMap;

    // email -> (key user token)
    private Map<String, KeyUserTokenTriple> oneUseRegistrationTokensMap;

    @Bean
    @Primary
    public static TrollsJwt makeTokenCreator(@Value("${token.key.retrieval}") final String retrieval,
                                             @Value("${token.key.iterations}") final Integer iterations) throws Exception {
        TrollsJwt token = new TrollsJwt();
        token.keys = new LinkedList<>();
        token.usernameToKeyTokenMap = new HashMap<>();
        token.tokenToKeyUsernameMap = new HashMap<>();
        token.oneUseRegistrationTokensMap = new HashMap<>();
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

    public boolean checkTokenForUser(final String user) {
        if (!usernameToKeyTokenMap.containsKey(user)) {
            log.error("I have no token in here for user {}", user);
            return false;
        }

        KeyTokenPair keyTokenPair = usernameToKeyTokenMap.get(user);

        try {
            Claims parsedToken = Jwts.parser().setSigningKey(keyTokenPair.getKey()).parseClaimsJws(keyTokenPair.getToken()).getBody();

            if (!parsedToken.get(USER_KEY).equals(user)) {
                log.error("User mismatch while verifying token for user {}", user);
                return false;
            }
        } catch (SignatureException exception) {
            log.error("Signature exception {}", exception.getMessage());
            return false;
        }

        return true;
    }

    public boolean checkHeaderWithToken(final String header) {
        String authorizationHeader = header.replace("Bearer ", "");
        return tokenToKeyUsernameMap.containsKey(authorizationHeader) &&
                checkTokenForUser(tokenToKeyUsernameMap.get(authorizationHeader).getUsername());

    }

    public String generateNewTokenForUser(final User user) {
        // Get a random key to sign our token
        String key = keys.get(new Random().nextInt(keys.size()));
        String generatedToken = Jwts.builder()
                .claim(USER_KEY, user.getBggNick())
                .claim(ROLE_KEY, user.getRole())
                .claim(AUTH_KEY, user.getAuthenticationType())
                .claim(EMAIL_KEY, user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 640_000_000L))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        usernameToKeyTokenMap.put(user.getBggNick(), new KeyTokenPair(key, generatedToken));
        tokenToKeyUsernameMap.put(generatedToken, new KeyUsernamePair(key, user.getBggNick()));
        return generatedToken;
    }

    public String getTokenForUser(final User user) {
        if (usernameToKeyTokenMap.containsKey(user.getBggNick()))
            return usernameToKeyTokenMap.get(user.getBggNick()).getToken();
        else
            return "";
    }

    public String generateRegistrationTokenForUser(final User user) {
        String key = keys.get(new Random().nextInt(keys.size()));
        String generatedToken = Jwts.builder()
                .claim("TEMP", true)
                .claim("EMAIL", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 200_000_000L))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        oneUseRegistrationTokensMap.put(user.getEmail(), new KeyUserTokenTriple(key, user, generatedToken));
        return generatedToken;
    }

    public User confirmRegistrationTokenForEmail(final String email, final String token) {
        if (oneUseRegistrationTokensMap.containsKey(email)) {
            KeyUserTokenTriple userTokenTriple = oneUseRegistrationTokensMap.get(email);
            String decodedEmail = (String) Jwts.parser().setSigningKey(userTokenTriple.getKey()).parseClaimsJws(token).getBody().get("EMAIL");
            if (decodedEmail.equalsIgnoreCase(userTokenTriple.getUser().getEmail())) {
                oneUseRegistrationTokensMap.remove(email);
                return userTokenTriple.getUser();
            }
        }

        return null;
    }

}
