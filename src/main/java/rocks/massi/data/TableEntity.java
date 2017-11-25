package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@ToString
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tables")
public class TableEntity {
    @Id
    @Column
    private int id;

    @Column
    private String name;

    @Column
    private int minPlayers;

    @Column
    private int maxPlayers;
}
