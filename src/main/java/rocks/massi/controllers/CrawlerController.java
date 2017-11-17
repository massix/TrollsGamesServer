package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.cache.CrawlCache;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.*;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.AuthenticationException;
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

    @Autowired
    private TrollsJwt trollsJwt;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers;

    private HashMap<String, Map.Entry<Thread, Runnable>> runningCrawlers() {
        if (runningCrawlers == null) runningCrawlers = new HashMap<>();
        return runningCrawlers;
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping("/games/{gameId}")
    public Game crawlGame(@RequestHeader("Authorization") final String authorization,
                          @PathVariable("gameId") final int gameId) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        return new CollectionCrawler(crawlCache,
                gamesRepository,
                ownershipsRepository,
                honorsRepository,
                gameHonorsRepository,
                null).crawlGame(gameId);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping("/collection/{user}")
    public void crawlCollectionForUser(@RequestHeader("Authorization") final String authorization,
                                       @PathVariable("user") final String nick, HttpServletResponse response) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

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

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @GetMapping("/queues")
    public List<CrawlingProgress> getQueues(@RequestHeader("Authorization") final String authorization) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        final List<CrawlingProgress> ret = new LinkedList<>();

        runningCrawlers().forEach((k, v) -> {
            CollectionCrawler collectionCrawler = (CollectionCrawler) v.getValue();
            CrawlingProgress crawlingProgress = collectionCrawler.getProgress();
            crawlingProgress.setQueue(v.getKey().getId());
            ret.add(crawlingProgress);
        });

        return ret;
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/queues")
    public List<CrawlingProgress> purgeFinishedQueues(@RequestHeader("Authorization") final String authorization) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

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

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @GetMapping("/queue/{id}")
    public CrawlingProgress getProgress(@RequestHeader("Authorization") final String authorization,
                                        @PathVariable("id") final long id, HttpServletResponse response) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

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

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/queue/{id}")
    public void deleteQueue(@RequestHeader("Authorization") final String authorization,
                            @PathVariable("id") final long id, HttpServletResponse response) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        CrawlingProgress progress = getProgress(authorization, id, response);
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
