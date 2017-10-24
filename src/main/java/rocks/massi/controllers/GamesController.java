package rocks.massi.controllers;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.Game;

import java.util.List;

@RestController
@RequestMapping("/v1/games")
public class GamesController {

    @Autowired
    private DatabaseConnector connector;

    @GetMapping("/get")
    public List<Game> getGames() {
        return connector.gameSelector.getGames();
    }

    @GetMapping("/get/{id}")
    public Game getGame(@PathVariable("id") final int id) {
        return connector.gameSelector.findById(id);
    }

    @PostMapping("/add")
    public Game insertGame(@RequestBody final Game game) {
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
