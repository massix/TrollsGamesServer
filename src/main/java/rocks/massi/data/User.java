package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class User {
    private String bggNick;
    private String forumNick;
    private String games;
    private String wants;
}
