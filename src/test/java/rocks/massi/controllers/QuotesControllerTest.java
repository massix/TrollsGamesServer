package rocks.massi.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Quote;
import rocks.massi.data.QuotesRepository;

import static junit.framework.TestCase.*;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuotesControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private QuotesRepository quotesRepository;

    @Before
    public void setUp() throws Exception {
        quotesRepository.deleteAll();
        quotesRepository.save(new Quote("Chakado", "J'aurais pas dû chier dans cette cage."));
        AuthorizationHandler.setUp(restTemplate);
    }

    @Test
    public void getRandomQuote() throws Exception {
        ResponseEntity<Quote> responseEntity = restTemplate.getForEntity("/v1/quotes/get/random", Quote.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody().getAuthor());
        assertNotNull(responseEntity.getBody().getQuote());
        assertEquals("Chakado", responseEntity.getBody().getAuthor());
        assertEquals("J'aurais pas dû chier dans cette cage.", responseEntity.getBody().getQuote());
    }

    @Test
    public void addQuote() throws Exception {
        quotesRepository.deleteAll();
        ResponseEntity<Quote> responseEntity = restTemplate.exchange("/v1/quotes/add", HttpMethod.PUT, new HttpEntity<>(new Quote("Random", "Quote")), Quote.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(1, quotesRepository.findAll().size());

        responseEntity = restTemplate.getForEntity("/v1/quotes/get/random", Quote.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("Random", responseEntity.getBody().getAuthor());
        assertEquals("Quote", responseEntity.getBody().getQuote());
    }

    @Test
    public void removeQuote() throws Exception {
        quotesRepository.save(new Quote("To be removed", "Quote"));
        ResponseEntity<Quote> responseEntity = restTemplate.exchange("/v1/quotes/remove?quote=Quote", HttpMethod.DELETE, null, Quote.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());

        responseEntity = restTemplate.getForEntity("/v1/quotes/get/random", Quote.class);
        assertEquals("Chakado", responseEntity.getBody().getAuthor());
        assertEquals("J'aurais pas dû chier dans cette cage.", responseEntity.getBody().getQuote());
    }

    @Test
    public void getAllQuotes() throws Exception {
        quotesRepository.save(new Quote("1", "1"));
        quotesRepository.save(new Quote("2", "2"));
        quotesRepository.save(new Quote("3", "3"));
        ResponseEntity<Quote[]> responseEntity = restTemplate.getForEntity("/v1/quotes/get", Quote[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(4, responseEntity.getBody().length);
    }
}