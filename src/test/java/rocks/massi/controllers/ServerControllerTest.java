package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Quote;
import rocks.massi.data.ServerInformation;
import rocks.massi.data.Stats;

import static org.junit.Assert.*;

@Slf4j
@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServerControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getServerInformation() throws Exception {
        ResponseEntity<ServerInformation> responseEntity = restTemplate.getForEntity("/v1/server/information", ServerInformation.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("TrollsGames", responseEntity.getBody().getArtifact());
    }

    @Test
    public void getRandomQuote() throws Exception {
        ResponseEntity<Quote> responseEntity = restTemplate.getForEntity("/v1/server/quote", Quote.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().getAuthor().isEmpty());
        assertFalse(responseEntity.getBody().getQuote().isEmpty());

        log.info("Got quote '{}' from '{}'", responseEntity.getBody().getQuote(), responseEntity.getBody().getAuthor());
    }

    @Test
    public void getStats() throws Exception {
        AuthorizationHandler.setUp(restTemplate);
        ResponseEntity<Stats[]> responseEntity = restTemplate.getForEntity("/v1/server/stats", Stats[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
    }
}