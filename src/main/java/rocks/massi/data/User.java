package rocks.massi.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;

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

    @Column
    @NonNull
    private String password = "";

    @Column
    private final String email;

    @Column
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column
    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType = AuthenticationType.JWT;

    @Column
    private boolean bggHandled = true;

    @JsonIgnore
    @OrderBy(value = "name")
    @ManyToMany
    @JoinTable(name = "ownerships",
            joinColumns = @JoinColumn(name = "userid"),
            inverseJoinColumns = @JoinColumn(name = "gameid"))
    private List<Game> collection;

    public User() {
        this("", "", "");
    }
}
