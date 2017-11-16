package rocks.massi.controllers;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Game;
import rocks.massi.data.GamesRepository;
import rocks.massi.data.PagesInformation;
import rocks.massi.data.joins.Ownership;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.AuthenticationException;
import rocks.massi.exceptions.GameNotFoundException;
import rocks.massi.exceptions.MalformattedGameException;

import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping("/v1/games")
public class GamesController {

    @Autowired
    private GamesRepository gamesRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    @CrossOrigin
    @GetMapping("/get")
    public List<Game> getGames() {
        return gamesRepository.findAll();
    }

    @CrossOrigin
    @GetMapping("/get/{id}")
    public Game getGame(@PathVariable("id") final int id) {
        Game g = gamesRepository.findById(id);

        if (g == null)
            throw new GameNotFoundException();

        return g;
    }

    @CrossOrigin
    @GetMapping("/get/page/{number}")
    public List<Game> getPagedGames(@PathVariable("number") final int pageNumber) {
        return gamesRepository.findAllByOrderByNameAsc(new PageRequest(pageNumber, 20)).getContent();
    }

    @CrossOrigin
    @GetMapping("/get/page/total")
    public PagesInformation getTotalPages() {
        return new PagesInformation(gamesRepository.findAll(new PageRequest(0, 20)).getTotalPages(), 20);
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping("/add")
    public Game insertGame(@RequestHeader("Authorization") final String authorization,
                           @RequestBody final Game game) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        if (game.getId() <= 0 || game.getName().isEmpty()) {
            throw new MalformattedGameException();
        }

        gamesRepository.save(game);
        return gamesRepository.findById(game.getId());
    }

    @DeleteMapping("/remove/{id}")
    public Game removeGame(@RequestHeader("Authorization") final String authorization,
                           @PathVariable("id") final int id) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized.");
        }

        val g = gamesRepository.findById(id);

        if (g != null) {
            gamesRepository.delete(g);
        }

        return g;
    }

    @CrossOrigin
    @GetMapping("/owners/{id}")
    public List<String> getOwnersForGame(@PathVariable("id") final int id) {
        List<Ownership> ret = ownershipsRepository.findByGame(id);
        List<String> users = new LinkedList<>();

        for (Ownership ownership : ret) {
            users.add(ownership.getUser());
        }

        return users;
    }
}
