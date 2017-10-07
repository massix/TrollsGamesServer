package rocks.massi.data;

import lombok.*;

import java.util.LinkedList;
import java.util.List;

@Data
@RequiredArgsConstructor
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

    private List<String> authorsList;
    private List<String> expandsList;

    public void buildAuthorsList() {
        List<String> ret = new LinkedList<>();
        String[] list = authors.split(" ");
        for (val author : list) {
            ret.add(author);
        }
    }

    public void buildExpandsList() {
        List<String> ret = new LinkedList<>();
        String[] list = expands.split(" ");
        for (val expand : list) {
            ret.add(expand);
        }
    }
}
