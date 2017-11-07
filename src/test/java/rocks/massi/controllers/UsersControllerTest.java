package rocks.massi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;

import static org.junit.Assert.*;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersControllerTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestRestTemplate restTemplate;

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
    }

    @Test
    public void addUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/users/add", new User("new_bgg", "new_forum"), User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("new_bgg", responseEntity.getBody().getBggNick());
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