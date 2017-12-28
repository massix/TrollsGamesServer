package rocks.massi.data.joins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users_groups")
@IdClass(UsersGroups.UsersGroupsKey.class)
public class UsersGroups {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UsersGroupsKey implements Serializable {
        private String userId;
        private Integer groupId;
    }

    public enum UserRole {
        ADMINISTRATOR,
        MEMBER,
        GUEST
    }

    @Id
    @Column
    private String userId;

    @Id
    @Column
    private Integer groupId;

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;
}
