package rocks.massi.data.bggjson;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Data
@RequiredArgsConstructor
public class BGGGame {
    private final int gameId;

    private final String name;
    private final String description;
    private final String image;
    private final String thumbnail;

    private final int minPlayers;
    private final int maxPlayers;
    private final int playingTime;

    private final String[] mechanics;

    private boolean isExpansion;

    private final int yearPublished;
    private final double bggRating;
    private final double averageRating;
    private final int rank;

    private final String[] designers;

    @Data
    @RequiredArgsConstructor
    public static class Expands {
        private final String gameName;
        private final int gameId;
    }

    private final ArrayList<Expands> expands;
}
