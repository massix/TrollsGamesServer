package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @Column
    private String message;

    @Column
    private String author;

    @Column
    private Long groupId;

    @Column
    private Long eventId;

    @Column
    private Long gameId;

    @Column
    private Long messageId;

    @Column
    private Integer rating;

    @Column
    private Timestamp dateTime;
}
