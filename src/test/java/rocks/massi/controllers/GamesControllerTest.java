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

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
@RunWith(SpringRunner.class)
public class GamesControllerTest {

    @Autowired
    private DatabaseConnector connector;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        connector.baseSelector.dropTableGames();
        connector.baseSelector.dropTableUsers();

        connector.baseSelector.createTableGames();
        connector.baseSelector.createTableUsers();

        connector.gameSelector.insertGame(
                new Game(1, "Cyclades", "Game of Cyclades", 2, 18, 250,
                        2012, 1, false, "here", "Bruno Cathala", "")
        );
    }

    @After
    public void tearDown() throws Exception {
        connector.baseSelector.dropTableUsers();
        connector.baseSelector.dropTableGames();
    }

    @Test
    public void getGames() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/games/get", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getBody().length == 1);
        assertEquals(responseEntity.getBody()[0].getName(), "Cyclades");
    }

    @Test
    public void getGame() throws Exception {
        ResponseEntity<Game> responseEntity = restTemplate.getForEntity("/v1/games/get/1", Game.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().getName(), "Cyclades");
    }

    @Test
    public void insertGame() throws Exception {
        Game newGame = new Game(2, "Cyclades II", "New game of Cyclades",
                2, 24, 350, 2012, 2, false, "",
                "Bruno Cathala", "");
        ResponseEntity<Game> responseEntity = restTemplate.postForEntity("/v1/games/add", newGame, Game.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("Cyclades II", responseEntity.getBody().getName());
    }

    @Test
    public void removeGame() throws Exception {
        restTemplate.delete("/v1/games/remove/2");
    }

}