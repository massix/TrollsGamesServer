package rocks.massi.crawler;

import feign.Response;
import feign.jaxb.JAXBContextFactory;
import feign.jaxb.JAXBDecoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import rocks.massi.cache.CrawlCache;
import rocks.massi.data.*;
import rocks.massi.data.boardgamegeek.Boardgames;
import rocks.massi.data.boardgamegeek.Collection;
import rocks.massi.data.joins.GameHonor;
import rocks.massi.data.joins.GameHonorsRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.services.BoardGameGeek;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;

@Slf4j
@Component
public class CollectionCrawler implements Runnable {

    @Autowired
    private CrawlCache crawlCache;

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private HonorsRepository honorsRepository;

    @Autowired
    private GameHonorsRepository gameHonorsRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private BoardGameGeek boardGameGeek;

    @Value("${bgg.url}")
    private String bggUrl;

    @Value("${crawl.timeout}")
    private int crawlTimeout;

    @Value("${crawl.batch.sleep}")
    private int batchSleep;

    private boolean running = true;
    private Thread runningThread;

    @Getter
    private Stack<User> usersToCrawl = new Stack<>();

    @Getter
    private Stack<Integer> gamesToCrawl = new Stack<>();

    @Getter
    private Stack<Ownership> ownershipsToCrawl = new Stack<>();

    private Set<Ownership> ownerships = new LinkedHashSet<>();

    private JAXBContextFactory contextFactory = new JAXBContextFactory.Builder().build();

    private long cacheHit = 0;
    private long cacheMiss = 0;
    private String started;
    private String finished;

    // Blocking method
    public boolean checkUserExists(User user) {
        try {
            Response r = boardGameGeek.getCollectionForUser(user.getBggNick());
            if (r.status() == 201 || r.status() == 202 || r.status() == 200) {
                addUserToCrawl(user);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    synchronized public void wakeUp() {
        if (!runningThread.isAlive()) {
            runningThread = new Thread(this);
            runningThread.start();
        }
    }

    public CollectionCrawler(@Value("${crawl.timeout}") int timeout) {
        this.crawlTimeout = timeout;

        runningThread = new Thread(this);
    }

    synchronized public void addUserToCrawl(final User user) {
        if (!usersToCrawl.contains(user)) {
            usersToCrawl.push(user);
            wakeUp();
        }
    }

    synchronized private void addGameToCrawl(final int gameId) {
        if (!gamesToCrawl.contains(gameId)) {
            gamesToCrawl.push(gameId);
            wakeUp();
        }
    }

    synchronized public void addOwnershipToCrawl(final Ownership ownership) {
        if (!ownershipsToCrawl.contains(ownership)) {
            ownershipsToCrawl.push(ownership);
            wakeUp();
        }
    }

    public Game crawlGame(final int gameId) {
        Boardgames.Boardgame boardgame = boardGameGeek.getGameForId(gameId).getBoardgame().get(0);
        Game toInsert = boardgame.convert();
        log.info("Crawled game {}", toInsert.getName());
        crawlCache.put(gameId, new Date().getTime() / 1000);

        gamesRepository.save(toInsert);

        // Save the honors (if any)
        if (boardgame.getHonors() != null) {
            boardgame.getHonors().forEach(honor -> {
                honorsRepository.save(new Honor(honor.getId(), honor.getDescription()));
                gameHonorsRepository.save(new GameHonor(honor.getId(), gameId));
            });
        }

        return gamesRepository.findById(gameId);
    }

    @Override
    public void run() {
        running = true;
        log.info("Targetting {} with a timeout of {}ms", bggUrl, crawlTimeout);
        started = new Date().toString();
        log.info("Sleeping for {}ms before starting the crawl", batchSleep);

        try {
            Thread.sleep(batchSleep);
        } catch (InterruptedException exception) {
            log.error("Couldn't sleep");
        }

        while (running && (!usersToCrawl.isEmpty() || !gamesToCrawl.isEmpty() || !ownershipsToCrawl.isEmpty())) {
            try {
                boolean didCrawlUser = false;
                boolean didCrawlGame = false;
                boolean didCrawlOwnership = false;

                if (!ownershipsToCrawl.isEmpty()) {
                    Ownership toCrawl = ownershipsToCrawl.pop();
                    log.info("Crawling ownership {}", toCrawl);

                    // We only crawl the game if the cache for it has expired or if we don't have it in DB
                    if (crawlCache.isExpired(toCrawl.getGame()) && (gamesRepository.findById(toCrawl.getGame()) == null)) {
                        crawlGame(toCrawl.getGame());
                        didCrawlOwnership = true;
                        cacheMiss++;
                    }

                    // Update game name
                    toCrawl.setGameName(gamesRepository.findById(toCrawl.getGame()).getName());

                    ownershipsRepository.save(toCrawl);
                }

                if (didCrawlOwnership) {
                    Thread.sleep(crawlTimeout);
                }

                if (!usersToCrawl.isEmpty()) {
                    User toCrawl = usersToCrawl.pop();
                    log.info("Crawling collection for user {}", toCrawl.getBggNick());

                    // If the user is handled via bgg we have to fetch his/her collection first.
                    if (toCrawl.isBggHandled()) {
                        log.info("{} is handled via bgg", toCrawl.getBggNick());
                        Response response = boardGameGeek.getCollectionForUser(toCrawl.getBggNick());
                        didCrawlUser = true;
                        if (response.status() != 200) {
                            log.info("Could not crawl user {}, status {}", toCrawl.getBggNick(), response.status());
                            usersToCrawl.push(toCrawl);
                        } else {
                            Collection collection = (Collection) new JAXBDecoder(contextFactory).decode(response, Collection.class);

                            // Get all the existing ownerships
                            List<Ownership> ownershipsInBase = ownershipsRepository.findByUser(toCrawl.getBggNick());

                            // Push all the games in the crawling list and in the ownerships structure
                            collection.getItemList().forEach(item -> {
                                Ownership potentialOwnership = new Ownership(toCrawl.getBggNick(), item.getId());

                                // Owned games should be pushed in the base and a new ownership created (after the game has been crawled).
                                if (item.getStatus().isOwn()) {
                                    ownerships.add(potentialOwnership);
                                }

                                // We will crawl all the games, just for fun.
                                addGameToCrawl(item.getId());
                            });

                            // Update the ownerships repository to reflect the new collection
                            ownershipsInBase.forEach(ownership -> {
                                if (!ownerships.contains(ownership)) {
                                    ownershipsRepository.deleteByUserAndGame(ownership.getUser(), ownership.getGame());
                                }
                            });

                            // Remove from the new ownerships all the remaining ones in the db
                            ownershipsInBase = ownershipsRepository.findByUser(toCrawl.getBggNick());
                            ownershipsInBase.forEach(ownership -> {
                                if (ownerships.contains(ownership)) {
                                    ownerships.remove(ownership);
                                }
                            });
                        }
                        // Else, we don't need to fetch the collection first, we will just recrawl all his/her games.
                    } else {
                        log.info("{} is handled manually, not crawling his/her collection, only crawling games", toCrawl.getBggNick());
                        ownershipsRepository.findByUser(toCrawl.getBggNick()).forEach(ownership -> {
                            addGameToCrawl(ownership.getGame());
                        });
                    }
                }

                if (didCrawlUser) {
                    Thread.sleep(crawlTimeout);
                }

                if (!gamesToCrawl.isEmpty()) {
                    int gameId = gamesToCrawl.pop();
                    if (crawlCache.isExpired(gameId)) {
                        crawlGame(gameId);
                        didCrawlGame = true;
                        cacheMiss++;
                    } else {
                        cacheHit++;
                    }

                    // Check if there are ownerships to update
                    for (Iterator<Ownership> it = ownerships.iterator(); it.hasNext(); ) {
                        Ownership ownership = it.next();
                        if (ownership.getGame() == gameId) {
                            ownership.setGameName(gamesRepository.findById(gameId).getName());
                            ownershipsRepository.save(ownership);
                            it.remove();
                        }
                    }
                }

                if (didCrawlGame) {
                    Thread.sleep(crawlTimeout);
                }

            } catch (IOException | InterruptedException e) {
                log.error("Exception caught while crawling: {}", e.getMessage());
            }
        }

        log.info("Storing cache");

        try {
            crawlCache.store();
        } catch (IOException e) {
            log.error("Unable to store cache to disk!");
        }

        finished = new Date().toString();
        log.info("Nothing to do!");
        running = false;
    }

    @PreDestroy
    public void stop() {
        log.info("Stopping crawler");
        this.running = false;
        try {
            log.info("Gracefully terminating thread");
            runningThread.join();
        } catch (InterruptedException exception) {
            log.error("Could not wait.");
        }
    }

    public CrawlerStatus getStatus() {
        List<String> usersLeft = new LinkedList<>();
        usersToCrawl.forEach(user -> usersLeft.add(user.getBggNick()));

        return new CrawlerStatus(runningThread.isAlive(), cacheHit, cacheMiss, usersLeft,
                gamesToCrawl.size(), ownershipsToCrawl.size(), started, finished);
    }
}
