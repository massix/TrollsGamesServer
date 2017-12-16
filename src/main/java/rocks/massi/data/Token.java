package rocks.massi.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import rocks.massi.authentication.TokenType;

import javax.persistence.*;

@Entity
@Table(name = "tokens")
@NoArgsConstructor
@Data
public class Token {
    @Column
    private String userEmail;

    @Column
    private String tokenValue;

    @Id
    @Column
    private String tokenKey;

    @Column
    @Enumerated(EnumType.STRING)
    private TokenType tokenType;
}
