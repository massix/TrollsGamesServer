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
import rocks.massi.data.Game;
import rocks.massi.data.User;

import static junit.framework.TestCase.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@RunWith(SpringRunner.class)
public class CollectionControllerTest {

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
        connector.gameSelector.insertGame(
                new Game(1, "Cyclades", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );
        connector.gameSelector.insertGame(
                new Game(2, "Cyclades II", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );
        connector.gameSelector.insertGame(
                new Game(3, "Cyclades III", "", 2, 18, 260, 2012,
                        1, false, "", "Bruno Cathala", "")
        );

    }

    @After
    public void tearDown() throws Exception {
        connector.baseSelector.dropTableUsers();
        connector.baseSelector.dropTableGames();
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

}