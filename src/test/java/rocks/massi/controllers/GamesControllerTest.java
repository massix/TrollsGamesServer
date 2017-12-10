package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
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
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@Slf4j
public class GamesControllerTest {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        gamesRepository.save(
                new Game(1, "Cyclades", "Game of Cyclades", 2, 18, 250,
                        2012, 1, false, "here", "Bruno Cathala", "")
        );

        usersRepository.save(new User("user_1", "", ""));
        usersRepository.save(new User("user_2", "", ""));

        Ownership o1 = new Ownership("user_1", 1);
        o1.setGameName("Cyclades");
        ownershipsRepository.save(o1);

        Ownership o2 = new Ownership("user_2", 1);
        o2.setGameName("Cyclades");
        ownershipsRepository.save(o2);

        AuthorizationHandler.setUp(restTemplate);
    }

    @After
    public void tearDown() throws Exception {
        ownershipsRepository.deleteAll();
        gamesRepository.deleteAll();
        usersRepository.deleteByBggNick("user_1");
        usersRepository.deleteByBggNick("user_2");
    }

    @Test
    public void getGames() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/games/get", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().length, 1);
        assertEquals(responseEntity.getBody()[0].getName(), "Cyclades");
    }

    @Test
    public void getGame() throws Exception {
        ResponseEntity<Game> responseEntity = restTemplate.getForEntity("/v1/games/get/1", Game.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().getName(), "Cyclades");
    }

    @Test
    public void getNonExistingGame() throws Exception {
        ResponseEntity<Game> responseEntity = restTemplate.getForEntity("/v1/games/get/666", Game.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
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
    public void insertMalformattedGame() throws Exception {
        Game newGame = new Game(0, "Cyclades II", "New game of Cyclades",
                2, 24, 350, 2012, 2, false, "",
                "Bruno Cathala", "");
        ResponseEntity<Game> responseEntity = restTemplate.postForEntity("/v1/games/add", newGame, Game.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());

        newGame = new Game(2, "", "New game of Cyclades",
                2, 24, 350, 2012, 2, false, "",
                "Bruno Cathala", "");
        responseEntity = restTemplate.postForEntity("/v1/games/add", newGame, Game.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void removeGame() throws Exception {
        restTemplate.delete("/v1/games/remove/2");
    }

    @Test
    public void getOwnersForGame() throws Exception {
        // Existing game, 2 users own it
        ResponseEntity<String[]> responseEntity = restTemplate.getForEntity("/v1/games/owners/1", String[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(2, responseEntity.getBody().length);
    }

    @Test
    public void getFuzzySearch() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/games/search?q=cycl", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("Cyclades", responseEntity.getBody()[0].getName());
    }
}
