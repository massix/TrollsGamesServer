package rocks.massi.data.joins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Data
@ToString
@Entity
@Table(name = "event_table")
@IdClass(EventTable.class)
@AllArgsConstructor
@NoArgsConstructor
public class EventTable implements Serializable {
    @Id
    @Column
    private int eventId;

    @Id
    @Column
    private int tableId;
}
