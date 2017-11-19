package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.PagesInformation;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.StatsLogger;

import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/collection")
public class CollectionController {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private StatsLogger statsLogger;

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public List<Game> getCollection(@RequestHeader("User-Agent") final String userAgent,
                                    @PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        LinkedList<Game> collection = new LinkedList<>();
        statsLogger.logStat("games/get/" + nick, userAgent);

        if (user != null) {
            List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick());
            ownerships.forEach(ownership -> collection.add(gamesRepository.findById(ownership.getGame())));
        }
        else {
            throw new UserNotFoundException("");
        }

        return collection;
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/page/{page}")
    public List<Game> getPagedCollection(@PathVariable("nick") final String nick,
                                         @PathVariable("page") final int page) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("");
        }

        List<Game> collection = new LinkedList<>();
        List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick(), new PageRequest(page, 20)).getContent();
        ownerships.forEach(ownership -> collection.add(gamesRepository.findById(ownership.getGame())));
        return collection;
    }

    @CrossOrigin
    @GetMapping("/get/{nick}/page/total")
    public PagesInformation getTotalPages(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        if (user == null) {
            throw new UserNotFoundException("");
        }

        return new PagesInformation(ownershipsRepository.findByUser(user.getBggNick(), new PageRequest(0, 20)).getTotalPages(), 20);
    }
}
