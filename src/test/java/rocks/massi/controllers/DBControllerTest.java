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

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class DBControllerTest {

    @Autowired
    private DatabaseConnector databaseConnector;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        databaseConnector.baseSelector.dropTableGames();
        databaseConnector.baseSelector.dropTableUsers();
    }

    @After
    public void tearDown() throws Exception {
        databaseConnector.baseSelector.dropTableUsers();
        databaseConnector.baseSelector.dropTableGames();
    }

    @Test
    public void createTables() throws Exception {
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity("/v1/dbcontroller/create", null, Void.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }

}