package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.*;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.StatsLogger;

import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
    private StatsLogger statsLogger;

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private CollectionCrawler collectionCrawler;

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public List<Game> getCollection(@RequestHeader("User-Agent") final String userAgent,
                                    @PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        statsLogger.logStat("games/get/" + nick, userAgent);

        if (user == null) {
            throw new UserNotFoundException("");
        }

        return user.getCollection();
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/total")
    public CollectionInformation getCollectionInformation(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("User not found in DB");
        }

        return new CollectionInformation(user.getCollection().size());
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
        List<Ownership> ownerships = ownershipsRepository.findByUserOrderByGameName(user.getBggNick(), new PageRequest(page, 20)).getContent();
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

        return new PagesInformation(ownershipsRepository.findByUserOrderByGameName(user.getBggNick(), new PageRequest(0, 20)).getTotalPages(), 20);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PutMapping("/add/{nick}/{game}")
    public void addGameForUser(@RequestHeader("Authorization") String authorization,
                               @PathVariable("nick") String nick,
                               @PathVariable("game") int gameId,
                               HttpServletResponse servletResponse) {
        // Check authorization
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (!userInformation.getUser().equals(nick) && userInformation.getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        // Check that the user exists
        if (getUser(usersRepository, nick) == null) {
            throw new UserNotFoundException("User not found in DB");
        }

        // Queue the game in the priority list in the crawler
        servletResponse.setStatus(HttpServletResponse.SC_ACCEPTED);
        collectionCrawler.addOwnershipToCrawl(new Ownership(nick, gameId));
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/remove/{nick}/{game}")
    public void removeGameForUser(@RequestHeader("Authorization") String authorization,
                                  @PathVariable("nick") String nick,
                                  @PathVariable("game") int gameId) {
        // Check authorization
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (!userInformation.getUser().equals(nick) && userInformation.getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
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

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @GetMapping("/search")
    public List<Game> searchGameInCollection(@RequestHeader("Authorization") String authorization,
                                             @RequestParam("query") String query,
                                             @Param("user") String user) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);

        // TODO: filter for groups here when groups will be a thing

        String calculatedUser = StringUtils.isEmpty(user) ? userInformation.getUser() : user;
        User u = usersRepository.findByBggNick(calculatedUser);
        List<Game> games = u.getCollection();

        // Calculate fuzzy search and get all the results with a score of 50% or more
        List<String> gamesNames = new LinkedList<>();
        games.forEach(g -> gamesNames.add(g.getName()));

        // Heck. Is there a better way to do this?
        //noinspection ConstantConditions
        return FuzzySearch.extractAll(query, gamesNames, 50).stream()
                .sorted(Comparator.comparingInt(ExtractedResult::getScore).reversed())
                .map(er -> games.stream().filter(
                        g -> g.getName().equals(er.getString())).findFirst().get())
                .collect(Collectors.toList());
    }
}
