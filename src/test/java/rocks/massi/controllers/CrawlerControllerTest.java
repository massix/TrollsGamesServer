package rocks.massi.controllers;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlingProgress;
import rocks.massi.data.User;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CrawlerControllerTest {

    @Autowired
    private DatabaseConnector databaseConnector;

    @Autowired
    private TestRestTemplate restTemplate;

    WireMockServer server;

    @Before
    public void setUp() throws Exception {
        databaseConnector.baseSelector.dropTableGames();
        databaseConnector.baseSelector.dropTableUsers();
        databaseConnector.baseSelector.createTableGames();
        databaseConnector.baseSelector.createTableUsers();

        databaseConnector.userSelector.addUser(new User("bgg_user", "forum_user", "68448 111661", ""));

        // Force purge cache
        restTemplate.delete("/v1/cache/purge");

        // Setup WireMock
        CrawlerController.BASE_URL = "http://localhost:8080";
        CollectionCrawler.BGG_BASE_URL = "http://localhost:8080";

        server = new WireMockServer(8080);
        server.start();

        server.stubFor(get((urlEqualTo("/xmlapi/boardgame/68448?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders.xml")));
        server.stubFor(get((urlEqualTo("/xmlapi/boardgame/111661?stats=1")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/7wonders_cities.xml")));

        server.stubFor(get((urlEqualTo("/collection/new_user")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/Chakado.json")));

    }

    @After
    public void tearDown() throws Exception {
        databaseConnector.baseSelector.dropTableUsers();
        databaseConnector.baseSelector.dropTableGames();
        server.stop();
    }

    @Test
    public void test1_crawlCollectionForUser() throws Exception {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/v1/crawler/collection/bgg_user", null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getHeaders().containsKey("location"));
        assertTrue(responseEntity.getHeaders().get("location").get(0).startsWith("/v1/crawler/queue"));

        // Wait for the Q to be over.
        Thread.sleep(1500);
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
        databaseConnector.userSelector.addUser(new User("new_user", "forum_user", "", ""));
        ResponseEntity<User> responseEntity = restTemplate.postForEntity("/v1/crawler/users/new_user", null, User.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        responseEntity.getBody().buildCollection();
        assertEquals(2, responseEntity.getBody().getCollection().size());
    }
}