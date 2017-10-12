package rocks.massi.services;

import feign.Param;
import feign.RequestLine;
import rocks.massi.data.bgg.BGGGame;
import rocks.massi.data.bgg.Collection;

public interface BGGJsonProxy {
    @RequestLine("GET /collection/{nick}")
    Collection getCollectionForUser(@Param("nick") final String user);

    @RequestLine("GET /thing/{id}")
    BGGGame getGameForId(@Param("id") final int gameId);
}
