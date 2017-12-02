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
import rocks.massi.data.TableEntity;
import rocks.massi.data.TablesRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
@Slf4j
public class TablesControllerTest {

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        tablesRepository.save(new TableEntity(0, "Table for event", 2, 8));
        AuthorizationHandler.setUp(restTemplate);
    }

    @After
    public void tearDown() throws Exception {
        tablesRepository.deleteAll();
    }

    @Test
    public void getTables() throws Exception {
        tablesRepository.save(new TableEntity(1, "Another table", 2, 4));
        ResponseEntity<TableEntity[]> responseEntity = restTemplate.getForEntity("/v1/tables/get", TableEntity[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().length, 2);
    }

    @Test
    public void getTable() throws Exception {
        ResponseEntity<TableEntity> responseEntity = restTemplate.getForEntity("/v1/tables/get/0", TableEntity.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(responseEntity.getBody().getName(), "Table for event");

        responseEntity = restTemplate.getForEntity("/v1/tables/get/12", TableEntity.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
    }

    @Test
    public void createTable() throws Exception {
        // Create a new table, passing id == 0 should auto-increment it on server side.
        restTemplate.put("/v1/tables/create", new TableEntity(0, "Another table", 1, 3));
        ResponseEntity<TableEntity[]> responseEntity = restTemplate.getForEntity("/v1/tables/get", TableEntity[].class);
        assertEquals(responseEntity.getBody().length, 2);
        assertEquals("Another table", responseEntity.getBody()[1].getName());
    }

    @Test
    public void removeTable() throws Exception {
        // Store a new table
        tablesRepository.save(new TableEntity(1, "Highlander", 1, 10));

        // Remove the table id 0
        restTemplate.delete("/v1/tables/remove/0");
        ResponseEntity<TableEntity[]> responseEntity = restTemplate.getForEntity("/v1/tables/get", TableEntity[].class);
        assertEquals(1, responseEntity.getBody().length);
        assertEquals("Highlander", responseEntity.getBody()[0].getName());
    }

}