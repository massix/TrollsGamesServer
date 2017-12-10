package rocks.massi.data.joins;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "ownerships")
@IdClass(Ownership.OwnershipKey.class)
public class Ownership implements Serializable {

    @Id
    @Column(name = "userid")
    private final String user;

    @Id
    @Column(name = "gameid")
    private final int game;

    @Column
    private String gameName;

    @NoArgsConstructor
    public static class OwnershipKey implements Serializable {
        @Id
        @Column(name = "userid")
        private String user;

        @Id
        @Column(name = "gameid")
        private int game;
    }

    public Ownership() {
        this("", 0);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Ownership) {
            Ownership that = (Ownership) o;
            return game == that.game && user.equals(that.user);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, game);
    }
}
