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
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.*;
import rocks.massi.data.joins.*;

import java.sql.Timestamp;
import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RdvControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private EventTablesRepository eventTablesRepository;

    @Autowired
    private TableGamesRepository tableGamesRepository;

    @Autowired
    private TableUsersRepository tableUsersRepository;

    @Before
    public void setUp() throws Exception {
        // Create some fake data
        usersRepository.save(new User("some_user", "someUser", "user@massi.rocks"));
        gamesRepository.save(new Game(0, "Cyclades", "", 0, 1, 0, 0, 0, false, "", "", ""));
        eventsRepository.save(new Event(0, "Some random event", new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime())));
        tablesRepository.save(new Table(0, "Some random table", 2, 4));

        eventTablesRepository.save(new EventTable(0, 0));
        tableGamesRepository.save(new TableGame(0, 0));
        tableUsersRepository.save(new TableUser(0, "some_user"));
        AuthorizationHandler.setUp(testRestTemplate);
    }

    @After
    public void tearDown() throws Exception {
        eventTablesRepository.deleteAll();
        tableGamesRepository.deleteAll();
        tableUsersRepository.deleteAll();

        tablesRepository.deleteAll();
        eventsRepository.deleteAll();
        gamesRepository.deleteAll();

        usersRepository.deleteByBggNick("some_user");
    }

    @Test
    public void getTablesForEvent() throws Exception {
        ResponseEntity<Table[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/event/0/tables", Table[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("Some random table", responseEntity.getBody()[0].getName());
    }

    @Test
    public void getGamesForTable() throws Exception {
        ResponseEntity<Game[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/games", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("Cyclades", responseEntity.getBody()[0].getName());
    }

    @Test
    public void getUsersForTable() throws Exception {
        ResponseEntity<User[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/users", User[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("some_user", responseEntity.getBody()[0].getBggNick());
        assertTrue(responseEntity.getBody()[0].getPassword().isEmpty());
    }

    @Test
    public void addUserToTable() throws Exception {
        usersRepository.save(new User("other_user", "otherUser", "otheruser@massi.rocks"));
        testRestTemplate.put("/v1/rdv/table/0/add_user/other_user", null);

        ResponseEntity<User[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/users", User[].class);
        assertEquals(2, responseEntity.getBody().length);
        assertEquals("other_user", responseEntity.getBody()[1].getBggNick());

        usersRepository.deleteByBggNick("other_user");
    }

    @Test
    public void addTableToEvent() throws Exception {
        tablesRepository.save(new Table(1, "random_table", 2, 18));
        testRestTemplate.put("/v1/rdv/event/0/add_table/1", null);

        ResponseEntity<Table[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/event/0/tables", Table[].class);
        assertEquals(2, responseEntity.getBody().length);
        assertEquals("random_table", responseEntity.getBody()[1].getName());

        tablesRepository.delete(1);
    }

    @Test
    public void addGameToTable() throws Exception {
        gamesRepository.save(new Game(1, "Cyclades le retour", "", 2, 18, 0, 0, 0, false, "", "", ""));
        testRestTemplate.put("/v1/rdv/table/0/add_game/1", null);

        ResponseEntity<Game[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/games", Game[].class);
        assertEquals(2, responseEntity.getBody().length);
        assertEquals("Cyclades le retour", responseEntity.getBody()[1].getName());
    }

    @Test
    public void removeUserFromTable() throws Exception {
        testRestTemplate.delete("/v1/rdv/table/0/remove_user/some_user");
        ResponseEntity<User[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/users", User[].class);
        assertEquals(0, responseEntity.getBody().length);
    }

    @Test
    public void removeGameFromTable() throws Exception {
        testRestTemplate.delete("/v1/rdv/table/0/remove_game/0");
        ResponseEntity<Game[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/table/0/games", Game[].class);
        assertEquals(0, responseEntity.getBody().length);
    }

    @Test
    public void removeTableFromEvent() throws Exception {
        testRestTemplate.delete("/v1/rdv/event/0/remove_table/0");
        ResponseEntity<Table[]> responseEntity = testRestTemplate.getForEntity("/v1/rdv/event/0/tables", Table[].class);
        assertEquals(0, responseEntity.getBody().length);
    }

}