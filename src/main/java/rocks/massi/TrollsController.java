package rocks.massi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.Game;
import rocks.massi.data.Response;

@RestController
public class TrollsController {

    @Autowired
    SQLiteConnector connector;

    @RequestMapping(value = "/v1/getGame/{id}", method = RequestMethod.GET)
    public Game getGame(@PathVariable("id") final int id) {
        return connector.gameSelector.findById(id);
    }

    @RequestMapping(value = "/v1/addGame", method = RequestMethod.POST)
    public Response insertGame(@RequestBody final Game game) {
        connector.gameSelector.insertGame(game);
        return new Response(false, "Ok.");
    }
}
