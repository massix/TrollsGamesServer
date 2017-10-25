package rocks.massi.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class CacheURL {
    private final String original;
    private final String protocol;
    private final String path;

    public CacheURL(final String original) {
        this.original = original;
        this.protocol = original.substring(0, original.indexOf("://"));
        this.path = original.substring(original.indexOf("//") + 2);

        log.info("Original: {}, protocol: '{}', path: '{}'", getOriginal(), getProtocol(), getPath());
    }
}
