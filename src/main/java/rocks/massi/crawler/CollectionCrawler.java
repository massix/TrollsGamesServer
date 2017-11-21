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
    private UsersRepository usersRepository;

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

    private boolean running = true;
    private Thread runningThread;

    @Getter
    private Stack<User> usersToCrawl = new Stack<>();

    @Getter
    private Stack<Integer> gamesToCrawl = new Stack<>();

    private Set<Ownership> ownerships = new LinkedHashSet<>();

    private long cacheHit = 0;
    private long cacheMiss = 0;
    private String started;
    private String finished;

    public void wakeUp() {
        if (!runningThread.isAlive()) {
            runningThread = new Thread(this);
            runningThread.start();
        }
    }

    public CollectionCrawler(@Value("${crawl.timeout}") int timeout) {
        this.crawlTimeout = timeout;

        runningThread = new Thread(this);
    }

    public void addUserToCrawl(final User user) {
        if (!usersToCrawl.contains(user)) {
            usersToCrawl.push(user);
            wakeUp();
        }
    }

    public void addGameToCrawl(final int gameId) {
        if (!gamesToCrawl.contains(gameId)) {
            gamesToCrawl.push(gameId);
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
        JAXBContextFactory contextFactory = new JAXBContextFactory.Builder().build();
        started = new Date().toString();

        while (running && (!usersToCrawl.isEmpty() || !gamesToCrawl.isEmpty())) {
            try {
                boolean didCrawlUser = false;
                boolean didCrawlGame = false;

                if (!usersToCrawl.isEmpty()) {
                    User toCrawl = usersToCrawl.pop();
                    log.info("Crawling collection for user {}", toCrawl.getBggNick());
                    Response response = boardGameGeek.getCollectionForUser(toCrawl.getBggNick());
                    didCrawlUser = true;
                    if (response.status() != 200) {
                        log.info("Could not crawl user {}, status {}", toCrawl.getBggNick(), response.status());
                        usersToCrawl.push(toCrawl);
                    } else {
                        Collection collection = (Collection) new JAXBDecoder(contextFactory).decode(response, Collection.class);

                        // Get all the existing ownerships
                        List<Ownership> ownershipList = ownershipsRepository.findByUser(toCrawl.getBggNick());

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
                        ownershipList.forEach(ownership -> {
                            if (!ownerships.contains(ownership)) {
                                ownershipsRepository.delete(ownership);
                            }
                        });

                        // Remove from the new ownerships all the remaining ones in the db
                        ownershipList = ownershipsRepository.findByUser(toCrawl.getBggNick());
                        ownershipList.forEach(ownership -> {
                            if (ownerships.contains(ownership)) {
                                ownerships.remove(ownership);
                            }
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
                gamesToCrawl.size(), started, finished);
    }
}
