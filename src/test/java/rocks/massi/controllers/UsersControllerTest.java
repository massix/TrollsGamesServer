package rocks.massi.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;

import static org.junit.Assert.*;
import static rocks.massi.authentication.TrollsJwt.ROLE_KEY;
import static rocks.massi.authentication.TrollsJwt.USER_KEY;

@Slf4j
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersControllerTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TrollsJwt trollsJwt;

    @Before
    public void setUp() throws Exception {
        usersRepository.deleteAll();
        usersRepository.save(new User("bgg_nick", "forum_nick"));
    }

    @Test
    public void getUserByNick() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/bgg_nick", User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("forum_nick", responseEntity.getBody().getForumNick());
    }

    @Test
    public void getAllUsers() throws Exception {
        ResponseEntity<User[]> responseEntity = restTemplate.getForEntity("/v1/users/get", User[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("bgg_nick", responseEntity.getBody()[0].getBggNick());
        assertEquals("*", responseEntity.getBody()[0].getPassword());
    }

    @Test
    public void addUser() throws Exception {
        User user = new User("new_bgg", "new_forum");
        user.setPassword("toto");
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());

        // Login
        user = new User("new_bgg", "");
        user.setPassword("toto");
        responseEntity = restTemplate.postForEntity("/v1/users/login", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getHeaders().containsKey("Authentication"));
        log.info("Received header Authentication : {}", responseEntity.getHeaders().get("Authentication"));

        // Check JWT token validity against the 'test' key
        String token = responseEntity.getHeaders().get("Authentication").get(0).replace("Bearer ", "");
        Claims parsedToken = Jwts.parser().setSigningKey("test").parseClaimsJws(token).getBody();
        assertEquals(parsedToken.get(USER_KEY), user.getBggNick());
        assertEquals(parsedToken.get(ROLE_KEY), Role.USER.toString());

        // Check TrollsJwt
        assertTrue(trollsJwt.checkTokenForUser(user));
    }

    @Test
    public void testWrongAuthentication() throws Exception {
        User user = new User("new_bgg", "new_forum");
        user.setPassword("toto");
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());

        // Wrong password
        user = new User("new_bgg", "");
        user.setPassword("dada");
        ResponseEntity<Void> responseEntityVoid = restTemplate.postForEntity("/v1/users/login", user, Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());

        // Wrong login
        responseEntityVoid = restTemplate.postForEntity("/v1/users/login", new User("not_exists", ""), Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());
    }

    @Test
    public void addMalformattedUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", new User("", "new_forum"), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());

        responseEntity = restTemplate.postForEntity("/v1/users/add", new User("new_bgg", ""), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());

        responseEntity = restTemplate.postForEntity("/v1/users/add", new User("", ""), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void removeUser() throws Exception {
        restTemplate.delete("/v1/users/remove/new_bgg");
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/new_bgg", User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void getNonExistingUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/non_existing", User.class);
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

}