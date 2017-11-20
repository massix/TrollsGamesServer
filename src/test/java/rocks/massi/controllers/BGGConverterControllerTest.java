package rocks.massi.controllers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.data.Game;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class BGGConverterControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8080);

    public BGGConverterControllerTest() {
        stubFor(get((urlEqualTo("/xmlapi/search?search=7+wonders")))
                .willReturn(aResponse().withStatus(200).withBodyFile("samples/search_7wonders.xml")));
    }

    @Test
    public void searchGame() throws Exception {
        ResponseEntity<Game[]> responseEntity = restTemplate.getForEntity("/v1/bggconverter/search?q=7 wonders", Game[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(31, responseEntity.getBody().length);
        assertEquals("7 Dice Wonders: Civilization", responseEntity.getBody()[1].getName());
    }

}