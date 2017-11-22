package rocks.massi.controllers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.*;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.OwnershipsRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CrawlerControllerTest {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private HonorsRepository honorsRepository;

    @Autowired
    private GameHonorsRepository gameHonorsRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8080);

    public CrawlerControllerTest() {
        log.info("--------- Setting up WireMock stubs");
        stubFor(get((urlEqualTo("/xmlapi/boardgame/68448?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders.xml")));
        stubFor(get((urlEqualTo("/xmlapi/boardgame/111661?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders_cities.xml")));

        stubFor(get((urlEqualTo("/xmlapi/collection/bgg_user")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/bgg_user.xml")));
        stubFor(get((urlEqualTo("/xmlapi/collection/bgg_user_two")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/bgg_user.xml")));

        stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(202).withBodyFile("samples/please_wait.xml")).willSetStateTo("SECOND"));
        stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("SECOND")
                .willReturn(aResponse().withStatus(202).withBodyFile("samples/please_wait.xml")).willSetStateTo("THIRD"));
        stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("THIRD")
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/bgg_user.xml")).willSetStateTo("END"));
    }

    @Before
    public void setUp() throws Exception {
        usersRepository.save(new User("bgg_user", "forum_user", "test_user1@example.com"));
        usersRepository.save(new User("bgg_user_two", "forum_user_two", "test_user2@example.com"));
        usersRepository.save(new User("timed_user", "timed_forum_user", "test_user3@example.com"));

        // Force purge cache
        restTemplate.delete("/v1/cache/purge");

        AuthorizationHandler.setUp(restTemplate, "test@example.com", "authorized_user");
    }

    @After
    public void tearDown() throws Exception {
        usersRepository.deleteAll();
        gamesRepository.deleteAll();
        honorsRepository.deleteAll();
        ownershipsRepository.deleteAll();
    }

    @SneakyThrows
    private void waitForQ() {
        // Wait for the Q to be over.
        ResponseEntity<CrawlerStatus> progress = restTemplate.getForEntity("/v1/crawler/status", CrawlerStatus.class);
        while (progress.getBody().isRunning()) {
            Thread.sleep(1000);
            progress = restTemplate.getForEntity("/v1/crawler/status", CrawlerStatus.class);
            log.info("Still running {}", progress.getBody());
        }
    }

    @Test
    public void test1_crawlCollectionForUser() throws Exception {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/v1/crawler/collection/bgg_user", null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        waitForQ();
        Thread.sleep(2000);

        // Check that honors have been inserted in the base
        Honor honor = honorsRepository.findById(19901);
        assertEquals(honor.getDescription(), "2012 Ludoteca Ideale Winner");
        assertEquals(51, honorsRepository.findAll().size());
        assertEquals(50, gameHonorsRepository.findByGame(68448).size());
        assertEquals(1, gameHonorsRepository.findByGame(111661).size());
        assertEquals(honorsRepository.findById(gameHonorsRepository.findByGame(111661).get(0).getHonor()).getDescription(),
                "2012 Golden Geek Best Board Game Expansion Nominee");
    }

    @Test
    public void test4_crawlNonExistingUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/crawler/collection/non_existing", null, User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void test5_timedUser() throws Exception {
        restTemplate.delete("/v1/crawler/queues");
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/v1/crawler/collection/timed_user", null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        waitForQ();

        assertEquals(2, ownershipsRepository.findByUser("timed_user").size());
        restTemplate.delete("/v1/crawler/queues");
    }

    @Test
    public void test6_multipleUsersSameGame() throws Exception {
        restTemplate.postForEntity("/v1/crawler/collection/bgg_user", null, Void.class);
        waitForQ();

        restTemplate.delete("/v1/crawler/queues");
        restTemplate.postForEntity("/v1/crawler/collection/bgg_user_two", null, Void.class);
        waitForQ();

        assertEquals(2, ownershipsRepository.findByUser("bgg_user").size());
        assertEquals(2, ownershipsRepository.findByUser("bgg_user_two").size());
    }
}