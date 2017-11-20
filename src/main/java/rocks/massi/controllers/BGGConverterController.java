package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.data.Game;
import rocks.massi.data.boardgamegeek.Boardgames;
import rocks.massi.services.BoardGameGeek;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/bggconverter")
public class BGGConverterController {

    @Autowired
    private BoardGameGeek connector;

    @CrossOrigin
    @GetMapping("/search")
    public List<Game> searchGame(@RequestParam("q") String query) {
        List<Game> games = new LinkedList<>();
        Boardgames searchResult = connector.search(query);
        searchResult.getBoardgame().forEach(game ->
                games.add(new Game(game.getId(), game.getAlternativeNames().get(0).getName(),
                        "", 0, 0, 0,
                        game.getYearPublished(),
                        0, false, "", "", ""))
        );

        return games;
    }
}
