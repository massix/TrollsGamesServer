package rocks.massi.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.persistence.*;
import java.util.LinkedList;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@RequiredArgsConstructor
public class User {
    @Id
    @Column(unique = true, name = "bggnick")
    @NonNull
    private final String bggNick;

    @Column(unique = true, name = "forumnick")
    @NonNull
    private final String forumNick;

    @Column
    @NonNull
    private final String games;

    @Column
    @NonNull
    private final String wants;

    @JsonIgnore
    @Transient
    private List<Integer> collection;

    public User() {
        this("", "", "", "");
    }

    public void buildCollection() {
        collection = new LinkedList<>();
        String[] gamesCollection = games.split(" ");
        for (val game : gamesCollection) {
            val gameId = Integer.valueOf(game);
            collection.add(gameId);
        }
    }
}
