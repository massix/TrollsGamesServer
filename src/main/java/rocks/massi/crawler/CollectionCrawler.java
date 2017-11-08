package rocks.massi.crawler;

import feign.Feign;
import feign.FeignException;
import feign.Response;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import rocks.massi.cache.CrawlCache;
import rocks.massi.data.*;
import rocks.massi.data.boardgamegeek.Boardgames;
import rocks.massi.data.boardgamegeek.Collection;
import rocks.massi.data.joins.GameHonor;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.services.BoardGameGeek;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class CollectionCrawler implements Runnable {
    private final int FAILURE_TIMEOUT = 2000;
    private final int INITIAL_TIMEOUT = 1000;
    private final int TIMEOUT_INCREASE = 3000;
    private final int MAXIMUM_TIMEOUT = 10000;
    public static String BGG_BASE_URL = "https://www.boardgamegeek.com";

    private final CrawlCache cache;
    private final GamesRepository gamesRepository;
    private final OwnershipsRepository ownershipsRepository;
    private final HonorsRepository honorsRepository;
    private final GameHonorsRepository gameHonorsRepository;
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

    @Getter
    private int totalGames = 0;

    private List<Game> crawled;
    private List<Integer> failed;
    private List<Ownership> ownerships;

    JAXBContextFactory contextFactory;
    BoardGameGeek boardGameGeek;

    public CollectionCrawler(CrawlCache cache,
                             GamesRepository gamesRepository,
                             OwnershipsRepository ownershipsRepository,
                             HonorsRepository honorsRepository,
                             GameHonorsRepository gameHonorsRepository,
                             User user) {
        this.cache = cache;
        this.gamesRepository = gamesRepository;
        this.ownershipsRepository = ownershipsRepository;
        this.honorsRepository = honorsRepository;
        this.gameHonorsRepository = gameHonorsRepository;
        this.user = user;

        crawled = new LinkedList<>();
        failed = new LinkedList<>();
        ownerships = new LinkedList<>();
        started = new Date();
        finished = new Date();
        contextFactory = new JAXBContextFactory.Builder().build();
        boardGameGeek = Feign.builder().decoder(new JAXBDecoder(contextFactory)).target(BoardGameGeek.class, BGG_BASE_URL);
    }

    public Game crawlGame(final int gameId) {
        Boardgames boardgames = boardGameGeek.getGameForId(gameId);

        // Get only the first result
        Boardgames.Boardgame boardgame = boardgames.getBoardgame().get(0);
        Game toInsert = boardgame.convert();

        // Cache game
        cache.put(gameId, new Date().getTime() / 1000);

        // Save game in DB
        gamesRepository.save(toInsert);

        // If the game has honors, extract and store them in the db
        if (boardgame.getHonors() != null) {
            boardgame.getHonors().forEach(honor -> {
                honorsRepository.save(new Honor(honor.getId(), honor.getDescription()));
                gameHonorsRepository.save(new GameHonor(honor.getId(), toInsert.getId()));
            });
        }

        return gamesRepository.findById(gameId);
    }

    @SneakyThrows
    @Override
    public void run() {
        running = true;
        cacheHit = 0;
        cacheMiss = 0;
        Response response = null;
        LinkedList<Integer> integers = new LinkedList<>();

        int status = 0;
        while (status != 200) {
            response = boardGameGeek.getCollectionForUser(user.getBggNick());
            status = response.status();

            if (status != 200) {
                log.info("Have to wait... status code {}", status);
                Thread.sleep(2000);
            }

        }

        Collection collection = (Collection) new JAXBDecoder(contextFactory).decode(response, Collection.class);

        log.info("Original collection {}", collection.toString());
        log.info("Collection {}", collection.ownedAsString());
        log.info("Wanted {}", collection.wantedAsString());
        collection.getItemList().forEach(item -> {
            if (item.getStatus().isOwn()) {
                integers.add(item.getId());
            }
        });

        totalGames = integers.size();

        for (int gameId : integers) {
            try {
                if (cache.isExpired(gameId)) {
                    Game g = crawlGame(gameId);
                    crawled.add(g);
                    log.info("Added game {} for user {}", g.getName(), user.getBggNick());
                    ownershipsRepository.save(new Ownership(user.getBggNick(), gameId));
                    cacheMiss++;
                    Thread.sleep(550);
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
                log.warn("Generic exception caught: {}. Leaving!", exception.getMessage());
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
                Thread.sleep(550);
                ownershipsRepository.save(new Ownership(user.getBggNick(), gameId));
            } catch (final FeignException exception) {
                log.warn("Could not download game id {} ({}), timeout = {}", gameId, exception.status(), timeout);
                it = failed.iterator();
                timeout += TIMEOUT_INCREASE;
                if (timeout > MAXIMUM_TIMEOUT) timeout = MAXIMUM_TIMEOUT;
            } catch (final InterruptedException e) {
                log.error("Could not sleep because {}", e.getMessage());
            } catch (final Exception exception) {
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
                totalGames,
                getStarted().toString(),
                getFinished().toString());
    }
}
