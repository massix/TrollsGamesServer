package rocks.massi.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.data.ServerInformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

}