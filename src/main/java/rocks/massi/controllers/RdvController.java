package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.*;
import rocks.massi.data.joins.EventTable;
import rocks.massi.data.joins.EventTablesRepository;
import rocks.massi.data.joins.TableGamesRepository;
import rocks.massi.data.joins.TableUsersRepository;

import java.util.LinkedList;
import java.util.List;

/**
 * Handles all the associations between tables, events, users and games.
 */
@RestController
@RequestMapping("/v1/rdv")
public class RdvController {

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private EventTablesRepository eventTablesRepository;

    @Autowired
    private TableGamesRepository tableGamesRepository;

    @Autowired
    private TableUsersRepository tableUsersRepository;

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private UsersRepository usersRepository;

    /**
     * Gets tables for event.
     *
     * @param eventId the event id
     * @return the tables for event
     */
    @CrossOrigin
    @GetMapping("/event/{id}/tables")
    public List<Table> getTablesForEvent(@PathVariable("id") int eventId) {
        List<Table> tables = new LinkedList<>();
        List<EventTable> eventTables = eventTablesRepository.findByEventId(eventId);

        eventTables.forEach(et -> tables.add(tablesRepository.findOne(et.getTableId())));

        return tables;
    }

    /**
     * Gets games for table.
     *
     * @param tableId the table id
     * @return the games for table
     */
    @CrossOrigin
    @GetMapping("/table/{id}/games")
    public List<Game> getGamesForTable(@PathVariable("id") int tableId) {
        List<Game> games = new LinkedList<>();
        tableGamesRepository.findByTableId(tableId).forEach(tg -> games.add(gamesRepository.findById(tg.getGameId())));
        return games;
    }

    /**
     * Gets users for table.
     *
     * @param tableId the table id
     * @return the users for table
     */
    @CrossOrigin
    @GetMapping("/table/{id}/users")
    public List<User> getUsersForTable(@PathVariable("id") int tableId) {
        List<User> users = new LinkedList<>();
        tableUsersRepository.findAll().forEach(tu -> {
            User user = usersRepository.findByBggNick(tu.getUserId());
            user.setPassword("");
            users.add(user);
        });

        return users;
    }
}
