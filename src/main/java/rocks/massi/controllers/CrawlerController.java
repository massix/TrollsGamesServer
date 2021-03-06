package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlerStatus;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/v1/crawler")
@CrossOrigin(allowedHeaders = {"Authorization"})
public class CrawlerController {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private CollectionCrawler collectionCrawler;

    @PostMapping("/games/{gameId}")
    public Game crawlGame(@RequestHeader("Authorization") final String authorization,
                          @PathVariable("gameId") final int gameId) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        return collectionCrawler.crawlGame(gameId);
    }

    @PostMapping("/collection/{user}")
    public void crawlCollectionForUser(@RequestHeader("Authorization") final String authorization,
                                       @PathVariable("user") final String nick, HttpServletResponse response) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        User user = DBUtils.getUser(usersRepository, nick);

        if (user != null) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            collectionCrawler.addUserToCrawl(user);
        } else {
            throw new UserNotFoundException("User does not exist in database");
        }
    }

    @GetMapping("/status")
    public CrawlerStatus getStatus(@RequestHeader("Authorization") final String authorization) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        return collectionCrawler.getStatus();
    }

    @PutMapping("/wake")
    public void wakeUp(@RequestHeader("Authorization") final String authorization) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        collectionCrawler.wakeUp();
    }

    @PutMapping("/stop")
    public void stop(@RequestHeader("Authorization") final String authorization) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        collectionCrawler.stop();
    }
}
