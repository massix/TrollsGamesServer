package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.UsersRepository;
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

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public List<Game> getCollection(@PathVariable("nick") final String nick) {
        val user = getUser(usersRepository, nick);
        LinkedList<Game> collection = new LinkedList<>();

        if (user != null) {
            user.buildCollection();
            user.getCollection().forEach(id -> collection.add(gamesRepository.findById(id)));
        }
        else {
            throw new UserNotFoundException("");
        }

        return collection;
    }
}
