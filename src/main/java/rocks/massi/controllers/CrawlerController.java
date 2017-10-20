package rocks.massi.controllers;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.cache.CrawlCache;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.bgg.BGGGame;
import rocks.massi.data.bgg.Collection;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.services.BGGJsonProxy;
import rocks.massi.utils.DBUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
@RestController
public class CrawlerController {

    private final String BASE_URL = "https://bgg-json.azurewebsites.net";
    private final int INITIAL_TIMEOUT = 1000;
    private final int TIMEOUT_INCREASE = 3000;
    private final int MAXIMUM_TIMEOUT = 10000;

    @Autowired
    private CrawlCache recentlyCrawled;

    @Autowired
    private SQLiteConnector connector;

    @RequestMapping(value = "/v1/crawler/crawl/users/{user}", method = RequestMethod.POST)
    public User crawlUser(@PathVariable("user") String user) {
        User userFromDb = DBUtils.getUser(connector, user);

        if (userFromDb == null) {
            throw new UserNotFoundException(String.format("User %s not found in DB.", user));
        }

        BGGJsonProxy bggJsonProxy = Feign.builder()
                .decoder(new GsonDecoder())
                .target(BGGJsonProxy.class, BASE_URL);

        Collection ownedGames = bggJsonProxy.getCollectionForUser(user);
        log.info("Original collection {}", ownedGames.toString());

        // Keep only wanted Games
        Collection wantedGames = (Collection) ownedGames.clone();
        wantedGames.removeIf(item -> ! item.isWantToPlay());

        // Remove non-owned games
        ownedGames.removeIf(item -> ! item.isOwned());

        log.info("Collection {}", ownedGames.toString());
        log.info("Wanted {}", wantedGames.toString());

        User updated = new User(userFromDb.getBggNick(), userFromDb.getForumNick(), ownedGames.toString(), wantedGames.toString());
        connector.userSelector.updateCollectionForUser(updated);

        return connector.userSelector.findByBggNick(updated.getBggNick());
    }

    @RequestMapping(value = "/v1/crawler/crawl/games/{gameId}", method = RequestMethod.POST)
    public Game crawlGame(@PathVariable("gameId") final int gameId) {
        BGGJsonProxy bggJsonProxy = Feign.builder().decoder(new GsonDecoder()).target(BGGJsonProxy.class, BASE_URL);

        BGGGame game = bggJsonProxy.getGameForId(gameId);
        recentlyCrawled.put(gameId, new Date().getTime() / 1000);

        try {
            recentlyCrawled.dumpToDisk();
        } catch (IOException e) {
            log.error("Could not dump to disk: {}", e.getMessage());
        }

        ArrayList<String> expands = new ArrayList<>();

        if (game.getExpands() != null)
            game.getExpands().forEach(expandsL -> expands.add(String.valueOf(expandsL.getGameId())));

        Game toInsert = new Game(game.getGameId(), game.getName(), game.getDescription(),
                game.getMinPlayers(), game.getMaxPlayers(), game.getPlayingTime(),
                game.getYearPublished(), game.getRank(), game.isExpansion(),
                game.getThumbnail(), String.join(", ", game.getDesigners()),
                String.join(" ", expands));

        Game inDb = connector.gameSelector.findById(game.getGameId());

        if (inDb != null) {
            connector.gameSelector.updateGame(toInsert);
        } else {
            connector.gameSelector.insertGame(toInsert);
        }

        return connector.gameSelector.findById(gameId);
    }

    @RequestMapping(value = "/v1/crawler/collection/{user}", method = RequestMethod.POST)
    public List<Game> crawlCollectionForUser(@PathVariable("user") final String nick) {
        User user = DBUtils.getUser(connector, nick);

        log.info("Starting crawl @ {}", new Date().getTime());

        if (user == null)
            throw new UserNotFoundException("User not found in DB.");

        user.buildCollection();
        List<Game> ret = new LinkedList<>();

        List<Integer> failed = new LinkedList<>();

        user.getCollection().forEach(gameId -> {
            boolean toBeCrawled = true;

            try {
                if (recentlyCrawled.containsKey(gameId)) {
                    long timestamp = recentlyCrawled.get(gameId);
                    long difference = (new Date().getTime() / 1000) - timestamp;
                    toBeCrawled = difference > recentlyCrawled.getCacheTTL();

                    log.info("Game {} has been crawled @ {}s ago, {} TTL: {}", gameId, difference,
                            toBeCrawled ? "refreshing it." : "not crawling it again.",
                            recentlyCrawled.getCacheTTL());
                }

                if (toBeCrawled) {
                    Game g = crawlGame(gameId);
                    ret.add(g);
                    log.info("Added game {} for user {}", g.getName(), user.getBggNick());
                }
            }
            catch (FeignException exception) {
                log.warn("Could not download game id {} ({})", gameId, exception.status());
                failed.add(gameId);
            }
        });

        int timeout = INITIAL_TIMEOUT;
        for (Iterator<Integer> it = failed.iterator(); it.hasNext(); ) {
            int gameId = it.next();
            try {
                log.info("Sleeping for {}s", ((double) timeout / 1000));
                Thread.sleep(timeout);
                Game g = crawlGame(gameId);
                ret.add(g);
                log.info("Added game {} for user {}", g.getName(), user.getBggNick());
                it.remove();
                timeout = INITIAL_TIMEOUT;
            }
            catch (final FeignException exception) {
                log.warn("Could not download game id {} ({}), timeout = {}", gameId, exception.status(), timeout);
                it = failed.iterator();
                timeout += TIMEOUT_INCREASE;
                if (timeout > MAXIMUM_TIMEOUT) timeout = MAXIMUM_TIMEOUT;
            } catch (final InterruptedException e) {
                log.error("Could not sleep because {}", e.getMessage());
            }
        }

        return ret;
    }
}
