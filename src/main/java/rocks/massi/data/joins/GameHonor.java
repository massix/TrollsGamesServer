package rocks.massi.data.joins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "game_honors")
public class GameHonor {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    @JsonIgnore
    private int id;

    @Column
    private final int honor;

    @Column
    private final int game;

    public GameHonor() {
        this(0, 0);
    }
}
