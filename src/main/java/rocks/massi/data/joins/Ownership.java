package rocks.massi.data.joins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "ownerships")
public class Ownership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @JsonIgnore
    private long id;

    @Column
    private final String user;

    @Column
    private final int game;

    public Ownership() {
        this("", 0);
    }
}
