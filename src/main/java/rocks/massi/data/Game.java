package rocks.massi.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "games")
@RequiredArgsConstructor
public class Game {

    @Id
    @Column
    private final int id;

    @Column
    @NonNull
    private final String name;

    @Column
    private final String description;

    @Column(name = "minplayers")
    private final int minPlayers;

    @Column(name = "maxplayers")
    private final int maxPlayers;

    @Column(name = "playingtime")
    private final int playingTime;

    @Column(name = "yearpublished")
    private final int yearPublished;

    @Column
    private final int rank;

    @Column
    private final boolean extension;

    @Column
    private final String thumbnail;

    @Column
    private final String authors;

    @Column
    private final String expands;

    public Game() {
        this(-1, "", "", 0, 0, 0, 0, 0, false, "", "", "");
    }
}
