package rocks.massi.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;

@Slf4j
@Data
@Component
public class CrawlCache {
    private URL cacheLocation;
    private int cacheTTL;
    private HashMap<Integer, Long> cache;
    private Jedis redis;

    public Long put(Integer key, Long value) {
        return cache.put(key, value);
    }

    public boolean containsKey(Integer key) {
        if (! cache.containsKey(key) && cacheLocation.getProtocol().equals("redis") && redis.exists(String.valueOf(key))) {
            Long value = Long.valueOf(redis.get(String.valueOf(key)));
            cache.put(key, value);
        }

        return cache.containsKey(key);
    }

    public Long get(Integer key) {
        return cache.get(key);
    }

    public void store() throws IOException {
        if (cacheLocation.getProtocol().equals("file")) {
            FileOutputStream outputStream = new FileOutputStream(cacheLocation.getPath());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(cache);
            objectOutputStream.close();
        }

        else if (cacheLocation.getProtocol().equals("redis")) {
            cache.forEach((k, v) -> redis.set(String.valueOf(k), String.valueOf(v)));
        }
    }

    @SuppressWarnings("unchecked")
    private static CrawlCache loadFromDisk(final URL cacheLocation, final int cacheTTL) {
        CrawlCache ret = new CrawlCache();

        try {
            FileInputStream inputStream = new FileInputStream(cacheLocation.getPath());
            ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
            ret.setCache((HashMap<Integer, Long>) objectInputStream.readObject());
            objectInputStream.close();

            ret.setCacheLocation(cacheLocation);
            ret.setRedis(null);

            /* Set Cache TTL in days */
            ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
        }

        catch (IOException | ClassNotFoundException exception) {
            log.warn("File {} not found on disk.", cacheLocation.getPath());
            ret.setCache(new HashMap<>());
            ret.setCacheLocation(cacheLocation);
            ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
            ret.setRedis(null);
        }

        return ret;
    }

    private static CrawlCache loadFromRedis(final URL cacheLocation, final Integer cacheTTL) {
        CrawlCache ret = new CrawlCache();
        ret.setCache(new HashMap<>());
        ret.setRedis(new Jedis(cacheLocation.toString()));
        ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
        return ret;
    }

    @Bean
    @Primary
    @SuppressWarnings("unused")
    public static CrawlCache loadCache(@Value("${crawler.cacheLocation}") final URL cacheLocation,
                                       @Value("${crawler.cacheTTL}") final Integer cacheTTL) {
        log.info("Loading cache @ {}", cacheLocation);
        log.info("Setting cache TTL @ {}", cacheTTL);

        if (cacheLocation.getProtocol().equals("file"))
            return loadFromDisk(cacheLocation, cacheTTL);
        else if (cacheLocation.getProtocol().equals("redis")) {
            return loadFromRedis(cacheLocation, cacheTTL);
        }
        else {
            log.warn("Couldn't read protocol, using in-memory cache!");
            CrawlCache ret = new CrawlCache();
            ret.setCacheLocation(cacheLocation);
            ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
            ret.setRedis(null);
            return ret;

        }
    }
}
