package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class CrawlerStatus {
    private final boolean running;
    private final long cacheHit;
    private final long cacheMiss;
    private final List<String> usersToCrawl;
    private final long gamesLeft;
    private final long ownershipsLeft;
    private final String started;
    private final String finished;
}
