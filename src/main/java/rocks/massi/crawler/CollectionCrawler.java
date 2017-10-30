package rocks.massi.crawler;

import feign.Feign;
import feign.FeignException;
import feign.gson.GsonDecoder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rocks.massi.cache.CrawlCache;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.CrawlingProgress;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.bgg.BGGGame;
import rocks.massi.data.bgg.Collection;
import rocks.massi.services.BGGJsonProxy;

import java.io.IOException;
import java.util.*;

@Slf4j
public class CollectionCrawler implements Runnable {
    private final int FAILURE_TIMEOUT = 2000;
    private final int INITIAL_TIMEOUT = 1000;
    private final int TIMEOUT_INCREASE = 3000;
    private final int MAXIMUM_TIMEOUT = 10000;
    public static String BASE_URL = "https://bgg-json.azurewebsites.net";

    private final CrawlCache cache;
    private final DatabaseConnector connector;
    private final User user;

    @Getter
    private int cacheHit;

    @Getter
    private int cacheMiss;

    @Getter
    private boolean running;

    @Getter
    private Date started;

    @Getter
    private Date finished;

    private List<Game> crawled;
    private List<Integer> failed;

    public CollectionCrawler(CrawlCache cache, DatabaseConnector connector, User user) {
        this.cache = cache;
        this.connector = connector;
        this.user = user;

        crawled = new LinkedList<>();
        failed = new LinkedList<>();
        started = new Date();
    }

    public Game crawlGame(final int gameId) {
        BGGJsonProxy bggJsonProxy = Feign.builder().decoder(new GsonDecoder()).target(BGGJsonProxy.class, BASE_URL);

        BGGGame game = bggJsonProxy.getGameForId(gameId);
        cache.put(gameId, new Date().getTime() / 1000);
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

    @Override
    public void run() {
        running = true;
        user.buildCollection();
        cacheHit = 0;
        cacheMiss = 0;

        for (int gameId : user.getCollection()) {
            try {
                if (cache.isExpired(gameId)) {
                    Game g = crawlGame(gameId);
                    crawled.add(g);
                    log.info("Added game {} for user {}", g.getName(), user.getBggNick());
                    cacheMiss++;
                } else {
                    log.info("No need to recrawl game {}", gameId);
                    cacheHit++;
                }
            } catch (FeignException exception) {
                log.warn("Could not download game id {} ({})", gameId, exception.status());
                log.warn("Sleeping for {}s", FAILURE_TIMEOUT / 1000);
                failed.add(gameId);
                try {
                    Thread.sleep(FAILURE_TIMEOUT);
                } catch (InterruptedException e) {
                    log.warn("Couldn't sleep.");
                }
            } catch (final Exception exception) {
                log.warn("Generic exception caught: {}. Leaving!");
                break;
            }
        }

        // Store intermediary cache
        try {
            cache.store();
        } catch (IOException e) {
            log.error("Could not dump to disk: {}", e.getMessage());
        }

        int timeout = INITIAL_TIMEOUT;
        for (Iterator<Integer> it = failed.iterator(); it.hasNext(); ) {
            int gameId = it.next();
            try {
                log.info("Sleeping for {}s", ((double) timeout / 1000));
                Thread.sleep(timeout);
                Game g = crawlGame(gameId);
                crawled.add(g);
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
            catch (final Exception exception) {
                log.error("Generic exception caught: {}. Leaving!", exception.getMessage());
                break;
            }
        }

        running = false;
        finished = new Date();

        // Store final cache
        try {
            cache.store();
        } catch (IOException e) {
            log.error("Could not dump to disk: {}", e.getMessage());
        }
    }

    public final CrawlingProgress getProgress() {
        return new CrawlingProgress(
                user.getBggNick(),
                running,
                crawled.size(),
                failed.size(),
                cacheHit,
                cacheMiss,
                user.getCollection().size(),
                getStarted().toString(),
                running? null : getFinished().toString());
    }
}
