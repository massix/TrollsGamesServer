package rocks.massi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlingProgress;
import rocks.massi.data.User;
import rocks.massi.data.bgg.BGGGame;
import rocks.massi.data.bgg.Collection;
import rocks.massi.services.BGGJsonProxy;

import javax.xml.ws.Response;
import java.util.ArrayList;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;

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

        Collection collection = new Collection();
        collection.add(new Collection.Item(1, "Cyclades", "", "", 2, 210,
            180, false, 2012, 9.9, 9.9, 1, 150, 9.9,
                true, false, false, false, false, false, false, false,
                "Best game ever."));
        collection.add(new Collection.Item(2, "Cyclades II", "", "", 2, 210,
                180, false, 2012, 9.9, 9.9, 1, 150, 9.9,
                true, false, false, false, false, false, false, false,
                "Best game ever."));

        // TODO: Yeah, I don't like this.
        String[] mechanics = {"mech"};
        ArrayList<BGGGame.Expands> expands = new ArrayList<>();
        BGGGame cyclades = new BGGGame(1, "Cyclades", "Cyclades", "", "",
                2, 20, 250, mechanics, 2012, 8.9, 9.9, 1, mechanics, expands);
        BGGGame cyclades2 = new BGGGame(2, "Cyclades 2", "Cyclades", "", "",
                2, 20, 250, mechanics, 2012, 8.9, 9.9, 1, mechanics, expands);;

        server = new WireMockServer(8080);
        server.start();

        ObjectMapper mapper = new ObjectMapper();

        CollectionCrawler.BASE_URL = "http://localhost:8080";
        server.stubFor(get((urlEqualTo("/collection/bgg_user")))
                .willReturn(aResponse().withStatus(200).withBody(mapper.writeValueAsString(collection))));

        // TODO: THERE HAS TO BE A BETTER WAY TO DO THIS
        server.stubFor(get((urlMatching("/thing/1")))
                .willReturn(aResponse().withStatus(200).withBody(mapper.writeValueAsString(cyclades))));
        server.stubFor(get((urlMatching("/thing/2")))
                .willReturn(aResponse().withStatus(200).withBody(mapper.writeValueAsString(cyclades2))));

        databaseConnector.userSelector.addUser(new User("bgg_user", "forum_user", "1 2", ""));

        // Force purge cache
        restTemplate.delete("/v1/cache/purge");
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
        Thread.sleep(2000);
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
}