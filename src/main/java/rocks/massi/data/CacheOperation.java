package rocks.massi.data;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class CacheOperation {

    @Data
    @RequiredArgsConstructor
    public static class CacheEntry {
        final int key;
        final long timestamp;
        final String humanReadable;
    }

    final boolean success;
    final String error;
    final List<CacheEntry> entries;
}
