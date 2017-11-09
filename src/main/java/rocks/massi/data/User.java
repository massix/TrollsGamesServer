package rocks.massi.data;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;

import javax.persistence.*;

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

    @NonNull
    @Column
    private String password = "";

    @Column
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column
    @Enumerated(EnumType.STRING)
    private AuthenticationType authenticationType = AuthenticationType.JWT;

    public User() {
        this("", "");
    }
}
