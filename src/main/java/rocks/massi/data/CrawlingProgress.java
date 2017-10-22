package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CrawlingProgress {
    private long queue;
    private final String user;
    private final boolean running;
    private final int crawled;
    private final int failed;
    private final int cacheHit;
    private final int cacheMiss;
    private final int total;
    private final String started;
    private final String finished;
}
