package rocks.massi.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.LoginInformation;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;

import static org.junit.Assert.*;
import static rocks.massi.authentication.TrollsJwt.ROLE_KEY;
import static rocks.massi.authentication.TrollsJwt.USER_KEY;

@Slf4j
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
public class UsersControllerTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestRestTemplate unauthorizedRestTemplate;

    @Autowired
    private TrollsJwt trollsJwt;

    @Before
    public void setUp() {
        usersRepository.save(new User("bgg_nick", "forum_nick", "test@example.com"));
        AuthorizationHandler.setUp(restTemplate);
    }

    @After
    public void tearDown() {
        usersRepository.deleteByBggNick("bgg_nick");
    }

    @Test
    public void test1_testWrongAuthentication() {
        User user = new User("new_bgg", "new_forum", "test_wrong_user@example.com");
        user.setPassword("toto");
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());

        // Wrong password
        LoginInformation loginInformation = new LoginInformation("test_wrong_user@example.com", "dada");
        ResponseEntity<Void> responseEntityVoid = restTemplate.postForEntity("/v1/users/login", loginInformation, Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());

        // Wrong login
        responseEntityVoid = restTemplate.postForEntity("/v1/users/login", new LoginInformation("test_non_existing@example.com", "pass"), Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());

        // Wrong authentication type
        user = usersRepository.findByEmail("test_wrong_user@example.com");
        user.setAuthenticationType(AuthenticationType.NONE);
        usersRepository.save(user);
        responseEntityVoid = restTemplate.postForEntity("/v1/users/login", new LoginInformation("test_wrong_user@example.com", "toto"), Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());

        usersRepository.deleteByBggNick("new_bgg");
    }

    @Test
    public void test2_addUser() {
        User user = new User("new_bgg", "new_forum", "test_user_new@example.com");
        user.setPassword("toto");
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());

        // Login
        user = new User("new_bgg", "", "test_user_new@example.com");
        user.setPassword("toto");
        responseEntity = restTemplate.postForEntity("/v1/users/login", user, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getHeaders().containsKey("Authorization"));
        log.info("Received header Authentication : {}", responseEntity.getHeaders().get("Authorization"));

        // Check JWT token validity against the 'test' key
        String token = responseEntity.getHeaders().get("Authorization").get(0).replace("Bearer ", "");
        Claims parsedToken = Jwts.parser().setSigningKey("test").parseClaimsJws(token).getBody();
        assertEquals(parsedToken.get(USER_KEY), user.getBggNick());
        assertEquals(parsedToken.get(ROLE_KEY), Role.ADMIN.toString());

        // Check TrollsJwt
        assertTrue(trollsJwt.checkTokenForUser(user.getEmail()));

        usersRepository.deleteByBggNick("new_bgg");
    }

    @Test
    public void test3_getUserByNick() {
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/bgg_nick", User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("forum_nick", responseEntity.getBody().getForumNick());
    }

    @Test
    public void test4_getAllUsers() {
        ResponseEntity<User[]> responseEntity = restTemplate.getForEntity("/v1/users/get", User[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(2, responseEntity.getBody().length);
        assertEquals("*", responseEntity.getBody()[0].getPassword());
    }


    @Test
    public void test5_addMalformattedUser() {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", new User("", "new_forum", "test@example.com"), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());

        responseEntity = restTemplate.postForEntity("/v1/users/add", new User("new_bgg", "", "test@example.com"), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());

        responseEntity = restTemplate.postForEntity("/v1/users/add", new User("", "", "test@example.com"), User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void test6_removeUser() {
        restTemplate.delete("/v1/users/remove/new_bgg");
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/new_bgg", User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void test7_getNonExistingUser() {
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/non_existing", User.class);
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

    @Test
    public void test8_modifyUser() {
        usersRepository.save(new User("to_modify", "some_forum_nick", "some_email@massi.rocks"));
        User newUser = new User("to_modify", "other_forum_nick", "other_mail@massi.rocks");
        newUser.setAuthenticationType(AuthenticationType.NONE);
        newUser.setRole(Role.USER);
        User responseEntity = restTemplate.patchForObject("/v1/users/modify", newUser, User.class);
        assertEquals("to_modify", responseEntity.getBggNick());
        assertEquals("other_forum_nick", responseEntity.getForumNick());
        assertEquals("other_mail@massi.rocks", responseEntity.getEmail());

        // We are enforcing JWT
        assertEquals(AuthenticationType.JWT, responseEntity.getAuthenticationType());
        assertEquals(Role.USER, responseEntity.getRole());
    }

    @Test
    public void test9_userInformationSecurity() {
        User dbUser = new User("unauthorized_user", "some_nick", "email@massi.rocks");

        // Set password "dadaumpa"
        dbUser.setPassword("$2a$04$YHVySpKbnMDXd0tXl8q2hOxWqMJrsyTk8rjUFJd1h2NEzZtvcwRM.");
        dbUser.setRole(Role.USER);
        usersRepository.save(dbUser);

        AuthorizationHandler.setUpNormalUser(unauthorizedRestTemplate, new LoginInformation("email@massi.rocks", "dadaumpa"));
        ResponseEntity<Void> responseEntityVoid = unauthorizedRestTemplate.getForEntity("/v1/users/get/massi_x/information", Void.class);
        assertTrue(responseEntityVoid.getStatusCode().is4xxClientError());

        ResponseEntity<TrollsJwt.UserInformation> responseEntity = unauthorizedRestTemplate.getForEntity("/v1/users/get/unauthorized_user/information", TrollsJwt.UserInformation.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().getUser(), "unauthorized_user");
        assertEquals(responseEntity.getBody().getEmail(), "email@massi.rocks");
        assertEquals(responseEntity.getBody().getRole(), Role.USER);
        assertEquals(responseEntity.getBody().getAuthenticationType(), AuthenticationType.JWT);

        usersRepository.deleteByBggNick("unauthorized_user");
    }

}