package rocks.massi.controllers;

import feign.Feign;
import feign.Response;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import rocks.massi.cache.CrawlCache;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlingProgress;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.boardgamegeek.Collection;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.services.BoardGameGeek;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Type;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v1/crawler")
public class CrawlerController {
    public static String BGG_BASE_URL = "https://www.boardgamegeek.com";

    @Autowired
    private DatabaseConnector connector;

    @Autowired
    private CrawlCache crawlCache;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers() {
        if (runningCrawlers == null) runningCrawlers = new HashMap<>();
        return runningCrawlers;
    }

    @SneakyThrows
    @PostMapping("/users/{user}")
    public User crawlUser(@PathVariable("user") String user) {
        User userFromDb = DBUtils.getUser(connector, user);

        if (userFromDb == null) {
            throw new UserNotFoundException(String.format("User %s not found in DB.", user));
        }

        int status = 0;
        JAXBContextFactory contextFactory = new JAXBContextFactory.Builder().build();
        BoardGameGeek boardGameGeek = Feign.builder().decoder(new JAXBDecoder(contextFactory)).target(BoardGameGeek.class, BGG_BASE_URL);
        Response response = null;

        while (status != 200) {
            response = boardGameGeek.getCollectionForUser(user);
            status = response.status();

            if (status != 200) {
                log.info("Have to wait... status code {}", status);
                Thread.sleep(5000);
            }

        }

        Collection collection = (Collection) new JAXBDecoder(contextFactory).decode(response, Collection.class);

        log.info("Original collection {}", collection.toString());
        log.info("Collection {}", collection.ownedAsString());
        log.info("Wanted {}", collection.wantedAsString());

        User updated = new User(userFromDb.getBggNick(), userFromDb.getForumNick(), collection.ownedAsString(), collection.wantedAsString());
        connector.userSelector.updateCollectionForUser(updated);

        return connector.userSelector.findByBggNick(updated.getBggNick());
    }

    @PostMapping("/games/{gameId}")
    public Game crawlGame(@PathVariable("gameId") final int gameId) {
        return new CollectionCrawler(crawlCache, connector, null).crawlGame(gameId);
    }

    @PostMapping("/collection/{user}")
    public void crawlCollectionForUser(@PathVariable("user") final String nick, HttpServletResponse response) {
        User user = DBUtils.getUser(connector, nick);

        if (user != null) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            Thread thread;

            if (!runningCrawlers().containsKey(user.getBggNick())) {
                CollectionCrawler collectionCrawler = new CollectionCrawler(crawlCache, connector, user);
                thread = new Thread(collectionCrawler);
                runningCrawlers().put(user.getBggNick(), new AbstractMap.SimpleEntry<>(thread, collectionCrawler));
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

    @GetMapping("/queues")
    public List<CrawlingProgress> getQueues() {
        final List<CrawlingProgress> ret = new LinkedList<>();

        runningCrawlers().forEach((k, v) -> {
            CollectionCrawler collectionCrawler = (CollectionCrawler) v.getValue();
            CrawlingProgress crawlingProgress = collectionCrawler.getProgress();
            crawlingProgress.setQueue(v.getKey().getId());
            ret.add(crawlingProgress);
        });

        return ret;
    }

    @DeleteMapping("/queues")
    public List<CrawlingProgress> purgeFinishedQueues() {
        List<CrawlingProgress> ret = new LinkedList<>();
        LinkedList<String> toBeRemoved = new LinkedList<>();

        runningCrawlers().forEach((k, v) -> {
            CollectionCrawler crawler = (CollectionCrawler) v.getValue();
            if (! crawler.isRunning()) {
                toBeRemoved.add(k);
                CrawlingProgress crawlingProgress = crawler.getProgress();
                crawlingProgress.setQueue(v.getKey().getId());
                ret.add(crawlingProgress);
            }
        });

        toBeRemoved.forEach(k -> runningCrawlers().remove(k));
        return ret;
    }

    @GetMapping("/queue/{id}")
    public CrawlingProgress getProgress(@PathVariable("id") final long id, HttpServletResponse response) {
        final CrawlingProgress[] progress = {null};
        runningCrawlers().forEach((k, v) -> {
            if (v.getKey().getId() == id) {
                CollectionCrawler crawler = (CollectionCrawler) v.getValue();
                progress[0] = crawler.getProgress();
                progress[0].setQueue(v.getKey().getId());
            }
        });

        if (progress[0] == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }

        return progress[0];
    }

    @DeleteMapping("/queue/{id}")
    public void deleteQueue(@PathVariable("id") final long id, HttpServletResponse response) {
        CrawlingProgress progress = getProgress(id, response);
        if (progress != null && ! progress.isRunning()) {
            runningCrawlers().remove(progress.getUser());
        }
        else if (progress == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
        else if (progress.isRunning()) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
    }
}
