package rocks.massi.controllers;

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
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@Slf4j
public class GamesControllerTest {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        gamesRepository.deleteAll();
        gamesRepository.save(
                new Game(1, "Cyclades", "Game of Cyclades", 2, 18, 250,
                        2012, 1, false, "here", "Bruno Cathala", "")
        );

        gamesRepository.findAll().forEach(game -> log.info("Found game {}", game.getName()));
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

}