package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.UserNotFoundException;

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

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public List<Game> getCollection(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        LinkedList<Game> collection = new LinkedList<>();

        if (user != null) {
            List<Ownership> ownerships = ownershipsRepository.findByUser(user.getBggNick());
            ownerships.forEach(ownership -> collection.add(gamesRepository.findById(ownership.getGame())));
        }
        else {
            throw new UserNotFoundException("");
        }

        return collection;
    }
}
