package rocks.massi.services;

import feign.Param;
import feign.RequestLine;
import rocks.massi.data.boardgamegeek.Boardgames;

public interface BoardGameGeek {
    @RequestLine("GET /xmlapi/boardgame/{id}?stats=1")
    Boardgames getGameForId(@Param("id") final int id);

    @RequestLine("GET /xmlapi/search?search={search}")
    Boardgames search(@Param("search") final String search);
}
