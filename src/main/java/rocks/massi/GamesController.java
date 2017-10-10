package rocks.massi;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.Game;

import java.util.List;

@RestController
public class GamesController {

    @Autowired
    private SQLiteConnector connector;

    @RequestMapping(value = "/v1/games/get", method = RequestMethod.GET)
    public List<Game> getGames() {
        return connector.gameSelector.getGames();
    }

    @RequestMapping(value = "/v1/games/get/{id}", method = RequestMethod.GET)
    public Game getGame(@PathVariable("id") final int id) {
        return connector.gameSelector.findById(id);
    }

    @RequestMapping(value = "/v1/games/add", method = RequestMethod.POST)
    public Game insertGame(@RequestBody final Game game) {
        connector.gameSelector.insertGame(game);
        return connector.gameSelector.findById(game.getId());
    }

    @RequestMapping(value = "/v1/games/remove/{id}", method = RequestMethod.DELETE)
    public Game removeGame(@PathVariable("id") final int id) {
        val g = connector.gameSelector.findById(id);

        if (g != null) {
            connector.gameSelector.removeGame(g);
        }

        return g;
    }
}
