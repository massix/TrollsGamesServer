package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.*;
import rocks.massi.data.joins.*;
import rocks.massi.exceptions.*;

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
        eventTablesRepository.findByEventId(eventId).forEach(et -> tables.add(tablesRepository.findOne(et.getTableId())));
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

    /**
     * Add an user to table.
     *
     * @param authorization the authorization token
     * @param tableId       the table id
     * @param bggNick       the bgg nick of the user
     * @return the join table->user
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/table/{id}/add_user/{user}")
    public TableUser addUserToTable(@RequestHeader("Authorization") String authorization,
                                    @PathVariable("id") int tableId,
                                    @PathVariable("user") String bggNick) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        if (tablesRepository.findOne(tableId) == null) {
            throw new TableNotFoundException();
        }

        if (usersRepository.findByBggNick(bggNick) == null) {
            throw new UserNotFoundException("");
        }

        return tableUsersRepository.save(new TableUser(tableId, bggNick));
    }

    /**
     * Add table to event.
     *
     * @param authorization the authorization token
     * @param eventId       the event id
     * @param tableId       the table id
     * @return the join event->table
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/event/{id}/add_table/{table}")
    public EventTable addTableToEvent(@RequestHeader("Authorization") String authorization,
                                      @PathVariable("id") int eventId,
                                      @PathVariable("table") int tableId) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        if (tablesRepository.findOne(tableId) == null) {
            throw new TableNotFoundException();
        }

        if (eventsRepository.findOne(eventId) == null) {
            throw new EventNotFoundException("");
        }

        return eventTablesRepository.save(new EventTable(eventId, tableId));
    }

    /**
     * Add game to table.
     *
     * @param authorization the authorization token
     * @param tableId       the table id
     * @param gameId        the game id
     * @return the join table->game
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/table/{id}/add_game/{gameId}")
    public TableGame addGameToTable(@RequestHeader("Authorization") String authorization,
                                    @PathVariable("id") int tableId,
                                    @PathVariable("gameId") int gameId) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        if (tablesRepository.findOne(tableId) == null) {
            throw new TableNotFoundException();
        }

        if (gamesRepository.findById(gameId) == null) {
            throw new GameNotFoundException();
        }

        return tableGamesRepository.save(new TableGame(tableId, gameId));
    }

    /**
     * Remove user from table table user.
     *
     * @param authorization the authorization
     * @param tableId       the table id
     * @param bggNick       the bgg nick
     * @return the table->user join
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/table/{id}/remove_user/{nick}")
    public TableUser removeUserFromTable(@RequestHeader("Authorization") String authorization,
                                         @PathVariable("id") int tableId,
                                         @PathVariable("nick") String bggNick) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        tableUsersRepository.delete(new TableUser(tableId, bggNick));
        return new TableUser(tableId, bggNick);
    }

    /**
     * Remove game from table table game.
     *
     * @param authorization the authorization
     * @param tableId       the table id
     * @param gameId        the game id
     * @return the table->game join
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/table/{id}/remove_game/{gameId}")
    public TableGame removeGameFromTable(@RequestHeader("Authorization") String authorization,
                                         @PathVariable("id") int tableId,
                                         @PathVariable("gameId") int gameId) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        tableGamesRepository.delete(new TableGame(tableId, gameId));
        return new TableGame(tableId, gameId);
    }

    /**
     * Remove table from event event table.
     *
     * @param authorization the authorization
     * @param eventId       the event id
     * @param tableId       the table id
     * @return the event->table join
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/event/{id}/remove_table/{tableId}")
    public EventTable removeTableFromEvent(@RequestHeader("Authorization") String authorization,
                                           @PathVariable("id") int eventId,
                                           @PathVariable("tableId") int tableId) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        eventTablesRepository.delete(new EventTable(eventId, tableId));
        return new EventTable(eventId, tableId);
    }
}
