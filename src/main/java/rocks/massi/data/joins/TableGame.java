package rocks.massi.data.joins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "table_game")
@IdClass(TableGame.class)
public class TableGame implements Serializable {
    @Id
    @Column
    private int tableId;

    @Id
    @Column
    private int gameId;
}
