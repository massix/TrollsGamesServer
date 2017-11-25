package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@ToString
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@javax.persistence.Table(name = "tables")
public class Table {
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
