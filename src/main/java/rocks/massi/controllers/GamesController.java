package rocks.massi.controllers;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.Game;
import rocks.massi.exceptions.GameNotFoundException;
import rocks.massi.exceptions.MalformattedGameException;

import java.util.List;

@RestController
@RequestMapping("/v1/games")
public class GamesController {

    @Autowired
    private DatabaseConnector connector;

    @CrossOrigin
    @GetMapping("/get")
    public List<Game> getGames() {
        return connector.gameSelector.getGames();
    }

    @CrossOrigin
    @GetMapping("/get/{id}")
    public Game getGame(@PathVariable("id") final int id) {
        Game g = connector.gameSelector.findById(id);

        if (g == null)
            throw new GameNotFoundException();

        return g;
    }

    @PostMapping("/add")
    public Game insertGame(@RequestBody final Game game) {
        if (game.getId() <= 0 || game.getName().isEmpty()) {
            throw new MalformattedGameException();
        }

        connector.gameSelector.insertGame(game);
        return connector.gameSelector.findById(game.getId());
    }

    @DeleteMapping("/remove/{id}")
    public Game removeGame(@PathVariable("id") final int id) {
        val g = connector.gameSelector.findById(id);

        if (g != null) {
            connector.gameSelector.removeGame(g);
        }

        return g;
    }
}
