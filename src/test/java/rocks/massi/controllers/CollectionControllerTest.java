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
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;

import static junit.framework.TestCase.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
public class CollectionControllerTest {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        usersRepository.save(new User("bgg_nick", "forum_nick", "test@example.com"));
        gamesRepository.save(
                new Game(1, "Cyclades", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );
        gamesRepository.save(
                new Game(2, "Cyclades II", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );
        gamesRepository.save(
                new Game(3, "Cyclades III", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );

        Ownership o1 = new Ownership("bgg_nick", 1);
        o1.setGameName("Cyclades");
        ownershipsRepository.save(o1);

        Ownership o2 = new Ownership("bgg_nick", 2);
        o2.setGameName("Cyclades II");
        ownershipsRepository.save(o2);

        Ownership o3 = new Ownership("bgg_nick", 3);
        o3.setGameName("Cyclades III");
        ownershipsRepository.save(o3);
    }

    @After
    public void tearDown() throws Exception {
        ownershipsRepository.deleteAll();
        gamesRepository.deleteAll();
        usersRepository.deleteByBggNick("bgg_nick");
    }

    @Test
    public void getCollection() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/collection/get/bgg_nick", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(3, responseEntity.getBody().length);

        assertEquals("Cyclades", responseEntity.getBody()[0].getName());
        assertEquals(260, responseEntity.getBody()[1].getPlayingTime());
        assertEquals("Cyclades III", responseEntity.getBody()[2].getName());
    }

    @Test
    public void getCollectionNonExistingUser() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/collection/get/non_existing", Game[].class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }
}