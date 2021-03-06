package rocks.massi.data.joins;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "table_user")
@IdClass(TableUser.class)
public class TableUser implements Serializable {
    @Id
    @Column
    private int tableId;

    @Id
    @Column
    private String userId;
}
