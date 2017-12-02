package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Entity
@Table(name = "quotes")
@IdClass(Quote.class)
public class Quote implements Serializable {
    @Id
    @Column
    private final String author;

    @Id
    @Column
    private final String quote;

    public Quote() {
        this("", "");
    }
}
