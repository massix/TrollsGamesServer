package rocks.massi.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Slf4j
@Component
public class Configuration {
    private final String cacheLocation;

    public Configuration(@Value("${crawler.cacheLocation}") final String cacheLocation) {
        log.info("Cache location: {}", cacheLocation);
        this.cacheLocation = cacheLocation;
    }
}
