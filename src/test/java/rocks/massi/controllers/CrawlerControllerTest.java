package rocks.massi.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
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
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.*;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

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

    WireMockServer server;

    @Before
    public void setUp() throws Exception {
        usersRepository.deleteAll();
        gamesRepository.deleteAll();
        honorsRepository.deleteAll();

        usersRepository.save(new User("bgg_user", "forum_user"));
        ownershipsRepository.save(new Ownership("bgg_user", 68448));
        ownershipsRepository.save(new Ownership("bgg_user", 111661));

        // Force purge cache
        restTemplate.delete("/v1/cache/purge");

        // Setup WireMock
        CrawlerController.BGG_BASE_URL = "http://localhost:8080";
        CollectionCrawler.BGG_BASE_URL = "http://localhost:8080";

        server = new WireMockServer(8080);
        server.start();

        server.stubFor(get((urlEqualTo("/xmlapi/boardgame/68448?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders.xml")));
        server.stubFor(get((urlEqualTo("/xmlapi/boardgame/111661?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders_cities.xml")));

        server.stubFor(get((urlEqualTo("/xmlapi/collection/new_user")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/massi_x.xml")));

        server.stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(202).withBodyFile("samples/please_wait.xml")).willSetStateTo("SECOND"));
        server.stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("SECOND")
                .willReturn(aResponse().withStatus(202).withBodyFile("samples/please_wait.xml")).willSetStateTo("THIRD"));
        server.stubFor(get((urlEqualTo("/xmlapi/collection/timed_user"))).inScenario("timed user").whenScenarioStateIs("THIRD")
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/massi_x.xml")).willSetStateTo("END"));
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void test1_crawlCollectionForUser() throws Exception {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/v1/crawler/collection/bgg_user", null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getHeaders().containsKey("location"));
        assertTrue(responseEntity.getHeaders().get("location").get(0).startsWith("/v1/crawler/queue"));

        // Wait for the Q to be over.
        Thread.sleep(5000);

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
    public void test2_getQueues() throws Exception {
        ResponseEntity<CrawlingProgress[]> responseEntity = restTemplate.getForEntity("/v1/crawler/queues", CrawlingProgress[].class);
        assertEquals(1, responseEntity.getBody().length);
        assertFalse(responseEntity.getBody()[0].isRunning());
    }

    @Test
    public void test3_purgeFinishedQueues() throws Exception {
        restTemplate.delete("/v1/crawler/queues");
        ResponseEntity<CrawlingProgress[]> responseEntity = restTemplate.getForEntity("/v1/crawler/queues", CrawlingProgress[].class);
        assertEquals(0, responseEntity.getBody().length);
    }

    @Test
    public void test4_crawlNonExistingUser() throws Exception {
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/crawler/users/non_existing", null, User.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void test5_crawlUser() throws Exception {
        usersRepository.save(new User("new_user", "forum_user_new"));
        ResponseEntity<Ownership[]> responseEntity = restTemplate.postForEntity("/v1/crawler/users/new_user", null, Ownership[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals(responseEntity.getBody().length, ownershipsRepository.findByUser("new_user").size());
    }

    @Test
    public void test6_timedUser() throws Exception {
        usersRepository.save(new User("timed_user", "forum_user_new"));
        ResponseEntity<Ownership[]> responseEntity = restTemplate.postForEntity("/v1/crawler/users/timed_user", null, Ownership[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals(responseEntity.getBody().length, ownershipsRepository.findByUser("timed_user").size());
    }
}