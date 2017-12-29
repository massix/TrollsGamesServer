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
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Message;
import rocks.massi.data.MessagesRepository;

import java.sql.Timestamp;
import java.util.Date;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotSame;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MessagesControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MessagesRepository messagesRepository;

    @Before
    public void setUp() {
        AuthorizationHandler.setUp(restTemplate);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void createNewMessage() {
        // Valid message
        ResponseEntity<Message> message = restTemplate.postForEntity("/v1/messages/create",
                new Message(0L, "This is a message from the future", "nope", null, null, null, null, null,
                        new Timestamp(new Date().getTime())), Message.class);
        assertEquals(200, message.getStatusCodeValue());
        assertEquals("massi_x", message.getBody().getAuthor());
        assertNotSame(0L, message.getBody().getMessageId());

        messagesRepository.deleteAll();
    }

    @Test
    public void getMessagesForHomepage() {

        // 10 messages for the homepage
        for (int i = 0; i < 10; i++) {
            messagesRepository.save(new Message(
                    null,
                    "This is a test message for the home page (" + i + ")",
                    "massi_x",
                    null, null, null, null, null,
                    new Timestamp(new Date().getTime())
            ));
        }

        // 1 for a group
        messagesRepository.save(new Message(
                null,
                "This is a message for a group",
                "massi_x",
                1L, null, null, null, null,
                new Timestamp(new Date().getTime())
        ));

        ResponseEntity<Message[]> messages = restTemplate.getForEntity("/v1/messages/homepage", Message[].class);
        assertEquals(200, messages.getStatusCodeValue());
        assertEquals(10, messages.getBody().length);

        // check that the order is right
        Message previous = null;
        for (Message current : messages.getBody()) {
            if (previous != null) assertTrue(current.getDateTime().before(previous.getDateTime()));
            previous = current;
        }

        messagesRepository.deleteAll();
    }
}