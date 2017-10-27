package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ServerInformation {
    private final String version;
    private final String artifact;
    private final String timestamp;
}
