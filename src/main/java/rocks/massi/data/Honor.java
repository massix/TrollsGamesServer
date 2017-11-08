package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "honors")
@Data
@RequiredArgsConstructor
public class Honor {
    @Id
    @Column(unique = true)
    private final int id;

    @Column
    private final String description;

    public Honor() {
        this(0, "");
    }
}
