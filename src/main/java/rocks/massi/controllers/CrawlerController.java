package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import rocks.massi.cache.CrawlCache;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.*;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/v1/crawler")
public class CrawlerController {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private HonorsRepository honorsRepository;

    @Autowired
    private GameHonorsRepository gameHonorsRepository;

    @Autowired
    private CrawlCache crawlCache;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers() {
        if (runningCrawlers == null) runningCrawlers = new HashMap<>();
        return runningCrawlers;
    }

    @PostMapping("/games/{gameId}")
    public Game crawlGame(@PathVariable("gameId") final int gameId) {
        return new CollectionCrawler(crawlCache,
                gamesRepository,
                ownershipsRepository,
                honorsRepository,
                gameHonorsRepository,
                null).crawlGame(gameId);
    }

    @PostMapping("/collection/{user}")
    public void crawlCollectionForUser(@PathVariable("user") final String nick, HttpServletResponse response) {
        User user = DBUtils.getUser(usersRepository, nick);

        if (user != null) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            Thread thread;

            if (!runningCrawlers().containsKey(user.getBggNick())) {
                CollectionCrawler collectionCrawler = new CollectionCrawler(crawlCache,
                        gamesRepository,
                        ownershipsRepository,
                        honorsRepository,
                        gameHonorsRepository,
                        user);
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
