package rocks.massi.data.bggjson;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.LinkedList;

@Data
@RequiredArgsConstructor
public class Collection extends LinkedList<Collection.Item> implements Serializable {

    @Data
    @RequiredArgsConstructor
    public static class Item {
        private final int gameId;
        private final String name;
        private final String image;
        private final String thumbnail;
        private final int minPlayers;
        private final int maxPlayers;
        private final int playingTime;

        @JsonProperty(value = "isExpansion")
        private final boolean expansion;

        private final int yearPublished;
        private final double bggRating;
        private final double averageRating;
        private final int rank;
        private final int numPlays;
        private final double rating;
        private final boolean owned;
        private final boolean preOrdered;
        private final boolean forTrade;
        private final boolean previousOwned;
        private final boolean want;
        private final boolean wantToPlay;
        private final boolean wantToBuy;
        private final boolean wishList;
        private final String userComment;
    }

    @Override
    public String toString() {
        LinkedList<String> strings = new LinkedList<>();
        forEach(item -> strings.add(String.valueOf(item.getGameId())));

        return String.join(" ", strings);
    }
}
