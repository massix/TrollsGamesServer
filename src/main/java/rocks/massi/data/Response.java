package rocks.massi.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response {
    private final boolean error;
    private final String response;
}
