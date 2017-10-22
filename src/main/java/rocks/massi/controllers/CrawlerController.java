package rocks.massi.controllers;

import feign.Feign;
import feign.gson.GsonDecoder;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.cache.CrawlCache;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlingProgress;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.bgg.Collection;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.services.BGGJsonProxy;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/crawler")
public class CrawlerController {
    private final String BASE_URL = "https://bgg-json.azurewebsites.net";

    @Autowired
    private SQLiteConnector connector;

    @Autowired
    private CrawlCache crawlCache;

    private HashMap<String, Pair<Thread, Runnable>> runningCrawlers;

    private HashMap<String, Pair<Thread, Runnable>> runningCrawlers() {
        if (runningCrawlers == null) runningCrawlers = new HashMap<>();
        return runningCrawlers;
    }

    @RequestMapping(value = "/users/{user}", method = RequestMethod.POST)
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

    @RequestMapping(value = "/games/{gameId}", method = RequestMethod.POST)
    public Game crawlGame(@PathVariable("gameId") final int gameId) {
        return new CollectionCrawler(crawlCache, connector, null).crawlGame(gameId);
    }

    @RequestMapping(value = "/collection/{user}", method = RequestMethod.POST)
    public void crawlCollectionForUser(@PathVariable("user") final String nick, HttpServletResponse response) {
        User user = DBUtils.getUser(connector, nick);

        if (user != null) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            Thread thread;

            if (!runningCrawlers().containsKey(user.getBggNick())) {
                CollectionCrawler collectionCrawler = new CollectionCrawler(crawlCache, connector, user);
                thread = new Thread(collectionCrawler);
                runningCrawlers().put(user.getBggNick(), new Pair<>(thread, collectionCrawler));
                thread.start();
            } else {
                thread = runningCrawlers().get(user.getBggNick()).getKey();
            }

            response.setHeader(HttpHeaders.LOCATION, "/v1/crawler/queue/" + String.valueOf(thread.getId()));
        }

        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @RequestMapping(value = "/queues", method = RequestMethod.GET)
    public List<Pair<Long, CrawlingProgress>> getQueues() {
        final List<Pair<Long, CrawlingProgress>> ret = new LinkedList<>();

        runningCrawlers().forEach((k, v) -> {
            CollectionCrawler collectionCrawler = (CollectionCrawler) v.getValue();
            ret.add(new Pair<>(v.getKey().getId(), collectionCrawler.getProgress()));
        });

        return ret;
    }

    @RequestMapping(value = "/queue/{id}", method = RequestMethod.GET)
    public CrawlingProgress getProgress(@PathVariable("id") final long id, HttpServletResponse response) {
        final CrawlingProgress[] progress = {null};
        runningCrawlers().forEach((k, v) -> {
            if (v.getKey().getId() == id) {
                CollectionCrawler crawler = (CollectionCrawler) v.getValue();
                progress[0] = crawler.getProgress();
            }
        });

        if (progress[0] == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return progress[0];
    }

    @RequestMapping(value = "/queue/{id}", method = RequestMethod.DELETE)
    public void deleteQueue(@PathVariable("id") final long id, HttpServletResponse response) {
        CrawlingProgress progress = getProgress(id, response);
        if (progress != null && ! progress.isRunning()) {
            runningCrawlers().remove(progress.getUser().getBggNick());
        }
        else if (progress == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else if (progress.isRunning()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
