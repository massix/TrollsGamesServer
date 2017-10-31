package rocks.massi.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.LinkedList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class User {
    @NonNull
    private final String bggNick;

    @NonNull
    private final String forumNick;

    @NonNull
    private final String games;

    @NonNull
    private final String wants;

    @JsonIgnore
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
