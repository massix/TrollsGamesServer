package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.LinkedList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class User {
    private final String bggNick;
    private final String forumNick;
    private final String games;
    private final String wants;

    private List<Integer> collection;

    public void buildCollection() {
        collection = new LinkedList<>();
        String[] gamesCollection = games.split(" ");
        for (val game : gamesCollection) {
            val gameId = Integer.valueOf(game);
            collection.add(gameId);
        }
    }
}
