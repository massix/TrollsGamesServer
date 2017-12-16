package rocks.massi.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import rocks.massi.data.Token;
import rocks.massi.data.TokensRepository;
import rocks.massi.data.User;

import java.util.*;

@Slf4j
@Data
@Component
public class TrollsJwt {

    @Autowired
    TokensRepository tokensRepository;

    @Getter
    @ToString
    @RequiredArgsConstructor
    public static class UserInformation {
        private final String user;
        private final Role role;
        private final AuthenticationType authenticationType;
        private final String email;
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

    @Bean
    @Primary
    public static TrollsJwt makeTokenCreator(@Value("${token.key.retrieval}") final String retrieval,
                                             @Value("${token.key.iterations}") final Integer iterations) throws Exception {
        TrollsJwt token = new TrollsJwt();
        token.keys = new LinkedList<>();
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

    public boolean checkTokenForUser(final String userEmail) {
        Token token = tokensRepository.findByUserEmail(userEmail);
        if (token == null) {
            log.error("I have no token in here for user {}", userEmail);
            return false;
        }

        try {
            Claims parsedToken = Jwts.parser().setSigningKey(token.getTokenKey()).parseClaimsJws(token.getTokenValue()).getBody();

            if (!parsedToken.get(EMAIL_KEY).equals(userEmail)) {
                log.error("User mismatch while verifying token for user {}", userEmail);
                return false;
            }
        } catch (SignatureException exception) {
            log.error("Signature exception {}", exception.getMessage());
            return false;
        }

        return true;
    }

    public UserInformation getUserInformationFromToken(final String header) {
        String tokenHeader = header.replace("Bearer ", "");
        Token token = tokensRepository.findByTokenValue(tokenHeader);
        if (token == null) {
            throw new TokenNotFoundException();
        }

        Claims parsedToken = Jwts.parser().setSigningKey(token.getTokenKey()).parseClaimsJws(token.getTokenValue()).getBody();

        return new UserInformation(parsedToken.get(USER_KEY, String.class),
                Role.valueOf(parsedToken.get(ROLE_KEY, String.class)),
                AuthenticationType.valueOf(parsedToken.get(AUTH_KEY, String.class)),
                parsedToken.get(EMAIL_KEY, String.class));
    }

    public String generateOrRetrieveTokenForUser(final User user) {

        Token existingToken = tokensRepository.findByUserEmail(user.getEmail());
        if (existingToken != null) {
            return existingToken.getTokenValue();
        }

        // Get a random key to sign our token
        String key = keys.get(new Random().nextInt(keys.size()));

        // Create a signed token
        String generatedToken = Jwts.builder()
                .claim(USER_KEY, user.getBggNick())
                .claim(ROLE_KEY, user.getRole())
                .claim(AUTH_KEY, user.getAuthenticationType())
                .claim(EMAIL_KEY, user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 640_000_000L))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        // Create new token
        Token token = new Token();
        token.setTokenKey(key);
        token.setTokenValue(generatedToken);
        token.setUserEmail(user.getEmail());
        token.setTokenType(TokenType.ACCESS);

        tokensRepository.save(token);

        return generatedToken;
    }

    public String generateRegistrationTokenForUser(final User user) {
        String key = keys.get(new Random().nextInt(keys.size()));
        String generatedToken = Jwts.builder()
                .claim("TEMP", true)
                .claim("EMAIL", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 200_000_000L))
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        Token token = new Token();
        token.setTokenType(TokenType.REGISTRATION);
        token.setUserEmail(user.getEmail());
        token.setTokenValue(generatedToken);
        token.setTokenKey(key);

        tokensRepository.save(token);

        return generatedToken;
    }

    public boolean confirmRegistrationTokenForEmail(final String email, final String token) {
        Token tokenDb = tokensRepository.findByUserEmail(email);
        if (tokenDb != null && tokenDb.getTokenType() == TokenType.REGISTRATION) {
            String decodedEmail = (String) Jwts.parser().setSigningKey(tokenDb.getTokenKey()).parseClaimsJws(token).getBody().get("EMAIL");
            if (decodedEmail.equalsIgnoreCase(tokenDb.getUserEmail())) {
                tokensRepository.delete(tokenDb);
                return true;
            }
        }

        return false;
    }

}
