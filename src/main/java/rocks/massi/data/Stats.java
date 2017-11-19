package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "stats")
@IdClass(Stats.StatsKey.class)
public class Stats {

    @Id
    @Column(name = "hashed_user")
    private final String hashedUser;

    @Id
    @Column
    private final String endpoint;

    @Column
    private final int counter;

    public Stats() {
        this("", "", 0);
    }

    @RequiredArgsConstructor
    public static class StatsKey implements Serializable {
        @Id
        @Column(name = "hashed_user")
        private final String hashedUser;

        @Id
        @Column
        private final String endpoint;

        public StatsKey() {
            this("", "");
        }
    }

}
