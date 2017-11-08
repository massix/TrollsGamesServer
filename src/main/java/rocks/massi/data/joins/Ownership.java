package rocks.massi.data.joins;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "ownerships")
@IdClass(Ownership.class)
public class Ownership implements Serializable {

    @Id
    @Column(name = "userid")
    private final String user;

    @Id
    @Column(name = "gameid")
    private final int game;

    public Ownership() {
        this("", 0);
    }
}
