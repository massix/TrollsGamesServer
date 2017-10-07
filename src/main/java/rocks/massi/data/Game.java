package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Game {
    private final int id;
    private final String name;
    private final String description;
    private final int minPlayers;
    private final int maxPlayers;
    private final int playingTime;
    private final int yearPublished;
    private final int rank;
    private final boolean extension;
    private final String thumbnail;
    private final String authors;
    private final String expands;
}
