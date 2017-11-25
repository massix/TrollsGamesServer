package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Event;
import rocks.massi.data.EventsRepository;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.EventNotFoundException;

import java.util.List;

/**
 * Handles retrieval, creation and suppression of events.
 */
@Slf4j
@RestController
@RequestMapping("/v1/events")
public class EventsController {

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    /**
     * Gets events.
     *
     * @return the events
     */
    @CrossOrigin
    @GetMapping("/get")
    public List<Event> getEvents() {
        return eventsRepository.findAll();
    }

    /**
     * Returns event with the given id.
     *
     * @param id the id
     * @return the event
     */
    @CrossOrigin
    @GetMapping("/get/{id}")
    public Event getEvent(@PathVariable("id") int id) {
        Event event = eventsRepository.findOne(id);
        log.info("Got event {}", event);
        if (event == null) {
            throw new EventNotFoundException("Event with id " + id + " does not exist.");
        } else {
            return event;
        }
    }

    /**
     * Create event event.
     *
     * @param authorization the authorization
     * @param event         the event
     * @return the event
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/create")
    public Event createEvent(@RequestHeader("Authorization") String authorization, @RequestBody Event event) {

        /**
         * TODO: we should just check that the user is logged in perhaps? Who will be given the responsibility
         *       of creating and handling the events?
         */
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        List<Event> allEvents = eventsRepository.findAll();

        // Find the event with the highest id
        if (allEvents.isEmpty()) {
            event.setId(0);
        } else {
            int newId = 0;
            for (Event e : allEvents) {
                if (e.getId() >= newId) {
                    newId = e.getId() + 1;
                }
            }

            event.setId(newId);
        }

        eventsRepository.save(event);
        return event;
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/remove/{id}")
    public Event removeEvent(@RequestHeader("Authorization") String authorization, @PathVariable("id") int id) {
        log.info("Removing event with id {}", id);
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        Event toBeRemoved = eventsRepository.findOne(id);
        if (toBeRemoved == null) {
            throw new EventNotFoundException("Event not found in base.");
        } else {
            eventsRepository.delete(id);
            return toBeRemoved;
        }
    }
}
