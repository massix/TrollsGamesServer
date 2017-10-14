package rocks.massi.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;

@Slf4j
@Data
@Component
public class CrawlCache extends HashMap<Integer, Long> {
    private String cacheLocation;
    private int cacheTTL;

    public void dumpToDisk() throws IOException {
        FileOutputStream outputStream = new FileOutputStream(cacheLocation);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(this);
        objectOutputStream.close();
    }

    @Bean
    @Primary
    public static CrawlCache readFromDisk(@Value("${crawler.cacheLocation}") final String cacheLocation,
                                          @Value("${crawler.cacheTTL}") final Integer cacheTTL) throws IOException, ClassNotFoundException {
        log.info("Loading cache @ {}", cacheLocation);
        log.info("Setting cache TTL @ {}", cacheTTL);

        try {
            FileInputStream inputStream = new FileInputStream(cacheLocation);
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            CrawlCache ret = (CrawlCache) objectInputStream.readObject();
            objectInputStream.close();

            ret.setCacheLocation(cacheLocation);

            /* Set Cache TTL in days */
            ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
            return ret;
        }

        catch (FileNotFoundException exc) {
            log.warn("File {} not found on disk.", cacheLocation);
            CrawlCache ret = new CrawlCache();
            ret.setCacheLocation(cacheLocation);
            ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
            return ret;
        }
    }
}
