package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.CrawlerStatus;
import rocks.massi.data.Game;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.exceptions.AuthenticationException;
import rocks.massi.exceptions.UserNotCrawlableException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("/v1/crawler")
public class CrawlerController {
    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private CollectionCrawler collectionCrawler;

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping("/games/{gameId}")
    public Game crawlGame(@RequestHeader("Authorization") final String authorization,
                          @PathVariable("gameId") final int gameId) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        return collectionCrawler.crawlGame(gameId);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping("/collection/{user}")
    public void crawlCollectionForUser(@RequestHeader("Authorization") final String authorization,
                                       @PathVariable("user") final String nick, HttpServletResponse response) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        User user = DBUtils.getUser(usersRepository, nick);

        if (user != null && user.isBggHandled()) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            collectionCrawler.addUserToCrawl(user);
        } else if (user != null && !user.isBggHandled()) {
            throw new UserNotCrawlableException("User is not handled via BGG");
        } else {
            throw new UserNotFoundException("User does not exist in database");
        }
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @GetMapping("/status")
    public CrawlerStatus getStatus(@RequestHeader("Authorization") final String authorization) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        return collectionCrawler.getStatus();
    }
}
