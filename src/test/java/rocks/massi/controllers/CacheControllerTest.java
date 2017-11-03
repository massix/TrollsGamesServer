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
import org.sqlite.SQLiteException;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.CacheOperation;

import static org.junit.Assert.*;

@ActiveProfiles("local")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CacheControllerTest {

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
    }

    @After
    public void tearDown() throws Exception {
        connector.baseSelector.dropTableUsers();
        connector.baseSelector.dropTableGames();
    }

    @Test
    public void purgeCache() throws Exception {
        restTemplate.delete("/v1/cache/purge");
    }

    @Test
    public void purgeExpired() throws Exception {
        restTemplate.delete("/v1/cache/expired");
    }

    @Test
    public void getMemoryCache() throws Exception {
        ResponseEntity<CacheOperation> responseEntity = restTemplate.getForEntity("/v1/cache/get", CacheOperation.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertTrue(responseEntity.getBody().isSuccess());
    }

}