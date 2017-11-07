package rocks.massi.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
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

    public User() {
        this("", "");
    }
}
