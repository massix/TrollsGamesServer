package rocks.massi.data.joins;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "game_honors")
@IdClass(GameHonor.class)
public class GameHonor implements Serializable {

    @Id
    @Column
    private final int honor;

    @Id
    @Column
    private final int game;

    public GameHonor() {
        this(0, 0);
    }
}
