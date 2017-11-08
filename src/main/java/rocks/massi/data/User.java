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
