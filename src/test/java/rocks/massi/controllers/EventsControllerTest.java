package rocks.massi.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Event;
import rocks.massi.data.EventsRepository;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class EventsControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventsRepository eventsRepository;

    @Before
    public void setUp() throws Exception {
        AuthorizationHandler.setUp(restTemplate);
        eventsRepository.save(new Event(0, "Soirée jeux",
                new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime() + 3600)));
    }

    @After
    public void tearDown() throws Exception {
        eventsRepository.deleteAll();
    }

    @Test
    public void getEvents() throws Exception {
        ResponseEntity<Event[]> responseEntity = restTemplate.getForEntity("/v1/events/get", Event[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals(1, responseEntity.getBody().length);

        // Test with empty base
        eventsRepository.deleteAll();
        responseEntity = restTemplate.getForEntity("/v1/events/get", Event[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertNotNull(responseEntity.getBody());
        assertEquals(0, responseEntity.getBody().length);
    }

    @Test
    public void getEvent() throws Exception {
        ResponseEntity<Event> responseEntity = restTemplate.getForEntity("/v1/events/get/0", Event.class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("Soirée jeux", responseEntity.getBody().getName());

        // Non-existing event
        responseEntity = restTemplate.getForEntity("/v1/events/get/14", Event.class);
        assertTrue(responseEntity.getStatusCode().is4xxClientError());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void createEvent() throws Exception {
        Event newEvent = new Event(1014, "Un nouvel evenement",
                new Timestamp(new Date().getTime()), new Timestamp(new Date().getTime()));
        restTemplate.put("/v1/events/create", newEvent);

        // Get all the events
        ResponseEntity<Event[]> responseEntity = restTemplate.getForEntity("/v1/events/get", Event[].class);
        assertEquals(2, responseEntity.getBody().length);

        // The events we have just created should have id == 1
        ResponseEntity<Event> singleResponseEntity = restTemplate.getForEntity("/v1/events/get/1", Event.class);
        assertTrue(singleResponseEntity.getStatusCode().is2xxSuccessful());
        assertEquals("Un nouvel evenement", singleResponseEntity.getBody().getName());

        // Event 1014 should not exist
        singleResponseEntity = restTemplate.getForEntity("/v1/events/get/1014", Event.class);
        assertTrue(singleResponseEntity.getStatusCode().is4xxClientError());

        eventsRepository.delete(1);
    }

    @Test
    public void removeEvent() throws Exception {
        // Remove event with id 0
        ResponseEntity<Event> firstRe = restTemplate.exchange("/v1/events/remove/0", HttpMethod.DELETE, null, Event.class);
        assertTrue(firstRe.getStatusCode().is2xxSuccessful());

        // Events should be empty now
        ResponseEntity<Event[]> responseEntity = restTemplate.getForEntity("/v1/events/get", Event[].class);
        assertTrue(responseEntity.getStatusCode().is2xxSuccessful());
        assertEquals(0, responseEntity.getBody().length);

        // Try removing a non-existing event
        ResponseEntity<Event> eventResponseEntity = restTemplate.exchange("/v1/events/remove/0", HttpMethod.DELETE, null, Event.class);
        assertTrue(eventResponseEntity.getStatusCode().is4xxClientError());
    }

}