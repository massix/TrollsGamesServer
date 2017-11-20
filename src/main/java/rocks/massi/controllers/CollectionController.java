package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.cache.CrawlCache;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.*;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.AuthenticationException;
import rocks.massi.exceptions.GameNotFoundException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.StatsLogger;

import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/collection")
public class CollectionController {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private GameHonorsRepository gameHonorsRepository;

    @Autowired
    private HonorsRepository honorsRepository;

    @Autowired
    private StatsLogger statsLogger;

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private CrawlCache crawlCache;

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public List<Game> getCollection(@RequestHeader("User-Agent") final String userAgent,
                                    @PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        LinkedList<Game> collection = new LinkedList<>();
        statsLogger.logStat("games/get/" + nick, userAgent);

        if (user != null) {
            List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick());
            ownerships.forEach(ownership -> collection.add(gamesRepository.findById(ownership.getGame())));
        }
        else {
            throw new UserNotFoundException("");
        }

        return collection;
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/total")
    public CollectionInformation getCollectioInformation(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("User not found in DB");
        }

        return new CollectionInformation(ownershipsRepository.findByUser(user.getBggNick()).size());
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/page/{page}")
    public List<Game> getPagedCollection(@PathVariable("nick") final String nick,
                                         @PathVariable("page") final int page) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("");
        }

        List<Game> collection = new LinkedList<>();
        List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick(), new PageRequest(page, 20)).getContent();
        ownerships.forEach(ownership -> collection.add(gamesRepository.findById(ownership.getGame())));
        return collection;
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/page/total")
    public PagesInformation getTotalPages(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("");
        }

        return new PagesInformation(ownershipsRepository.findByUser(user.getBggNick(), new PageRequest(0, 20)).getTotalPages(), 20);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/add/{nick}/{game}")
    public Ownership addGameForUser(@RequestHeader("Authorization") String authorization,
                                    @PathVariable("nick") String nick,
                                    @PathVariable("game") int gameId) {

        // Check authorization
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized");
        }

        // Check that the user exists
        if (getUser(usersRepository, nick) == null) {
            throw new UserNotFoundException("User not found in DB");
        }

        Game toBeAdded = gamesRepository.findById(gameId);
        if (toBeAdded == null) {
            try {
                toBeAdded = new CollectionCrawler(crawlCache,
                        gamesRepository,
                        ownershipsRepository,
                        honorsRepository,
                        gameHonorsRepository,
                        null).crawlGame(gameId);
            } catch (Exception e) {
                throw new GameNotFoundException();
            }
        }

        ownershipsRepository.save(new Ownership(nick, toBeAdded.getId()));
        return ownershipsRepository.findByUserAndGame(nick, gameId);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/remove/{nick}/{game}")
    public void removeGameForUser(@RequestHeader("Authorization") String authorization,
                                  @PathVariable("nick") String nick,
                                  @PathVariable("game") int gameId) {

        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized");
        }

        if (getUser(usersRepository, nick) == null) {
            throw new UserNotFoundException("User not found in DB");
        }

        Ownership ownership = ownershipsRepository.findByUserAndGame(nick, gameId);

        if (ownership == null) {
            throw new UserNotFoundException("Ownership not found on DB");
        }

        ownershipsRepository.delete(ownership);
    }
}
