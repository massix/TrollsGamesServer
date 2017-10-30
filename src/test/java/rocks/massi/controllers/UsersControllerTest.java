package rocks.massi.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.User;

import java.util.List;

import static org.junit.Assert.*;

@ActiveProfiles("local")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersControllerTest {

    @Autowired
    private DatabaseConnector connector;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        connector.baseSelector.dropTableGames();
        connector.baseSelector.dropTableUsers();
        connector.baseSelector.createTableUsers();
        connector.baseSelector.createTableGames();

        connector.userSelector.addUser(new User("bgg_nick", "forum_nick", "1 2 3", ""));
    }

    @After
    public void tearDown() throws Exception {
        connector.baseSelector.dropTableUsers();
        connector.baseSelector.dropTableGames();
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
    }

    @Test
    public void addUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", new User("new_bgg", "new_forum", "", ""), User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());
    }

    @Test
    public void removeUser() throws Exception {
        restTemplate.delete("/v1/users/remove/new_bgg");
        ResponseEntity<User> responseEntity = restTemplate.getForEntity("/v1/users/get/new_bgg", User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNull(responseEntity.getBody());
    }

}