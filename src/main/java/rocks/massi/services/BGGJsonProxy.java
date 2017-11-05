package rocks.massi.services;

import feign.Headers;
import feign.Param;
import feign.RequestLine;
import rocks.massi.data.bggjson.BGGGame;
import rocks.massi.data.bggjson.Collection;

public interface BGGJsonProxy {
    @RequestLine("GET /collection/{nick}")
    @Headers(value = {
            "User-Agent: TrollsGamesServer. Contact massimo.gengarelli@gmail.com"
    })
    Collection getCollectionForUser(@Param("nick") final String user);

    @RequestLine("GET /thing/{id}")
    @Headers(value = {
            "User-Agent: TrollsGamesServer. Contact massimo.gengarelli@gmail.com"
    })
    BGGGame getGameForId(@Param("id") final int gameId);
}
