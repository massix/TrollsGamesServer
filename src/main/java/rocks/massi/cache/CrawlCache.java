package rocks.massi.cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@Slf4j
@Data
@Component
public class CrawlCache {
    private CacheURL cacheLocation;
    private int cacheTTL;
    private HashMap<Integer, Long> cache;
    private Jedis redis;

    public Long put(Integer key, Long value) {
        return cache.put(key, value);
    }

    private boolean containsKey(Integer key) {
        if (! cache.containsKey(key) && cacheLocation.getProtocol().equals("redis") && redis.exists(String.valueOf(key))) {
            Long value = Long.valueOf(redis.get(String.valueOf(key)));
            cache.put(key, value);
        }

        return cache.containsKey(key);
    }

    public Long get(Integer key) {
        return cache.get(key);
    }

    public boolean isExpired(Integer key) {
        if (containsKey(key)) {
            long timestamp = get(key);
            long difference = (new Date().getTime() / 1000) - timestamp;
            return difference >= getCacheTTL();
        }

        return true;
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

    public void purgeAll() throws IOException {
        cache.clear();
        if (cacheLocation.getProtocol().equals("redis"))
            redis.flushAll();

        store();
    }

    public void purgeExpired() throws IOException {
        long now = new Date().getTime();
        for (HashMap.Entry<Integer, Long> entry : cache.entrySet()) {
            long diff = entry.getValue() - now;
            if (diff >= getCacheTTL()) {
                cache.remove(entry.getKey());

                if (cacheLocation.getProtocol().equals("redis"))
                    redis.del(String.valueOf(entry.getKey()));
            }
        }

        if (cacheLocation.getProtocol().equals("redis")) {
            Set<String> keys = redis.keys("*");
            keys.forEach(key -> {
                long diff = Long.valueOf(redis.get(key)) - now;
                if (diff >= getCacheTTL()) {
                    redis.del(key);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static CrawlCache loadFromDisk(final CacheURL cacheLocation, final int cacheTTL) {
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

    private static CrawlCache loadFromRedis(final CacheURL cacheLocation, final Integer cacheTTL) throws URISyntaxException {
        CrawlCache ret = new CrawlCache();
        log.info("Loading cache from Redis");
        ret.setCacheLocation(cacheLocation);
        ret.setCache(new HashMap<>());
        ret.setRedis(new Jedis(cacheLocation.getOriginal()));
        ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
        return ret;
    }

    @Bean
    @Primary
    @SuppressWarnings("unused")
    public static CrawlCache loadCache(@Value("${crawler.cacheLocation}") final String cacheLocation,
                                       @Value("${crawler.cacheTTL}") final Integer cacheTTL) throws URISyntaxException, MalformedURLException {
        log.info("Loading cache @ {}", cacheLocation);
        log.info("Setting cache TTL @ {}", cacheTTL);

        CacheURL dbUrl = new CacheURL(cacheLocation);
        switch (dbUrl.getProtocol()) {
            case "file":
                return loadFromDisk(dbUrl, cacheTTL);
            case "redis":
                return loadFromRedis(dbUrl, cacheTTL);
            default:
                log.warn("Couldn't read protocol, using in-memory cache!");
                CrawlCache ret = new CrawlCache();
                ret.setCacheLocation(dbUrl);
                ret.setCacheTTL(60 * 60 * 24 * cacheTTL);
                ret.setCache(new HashMap<>());
                ret.setRedis(null);
                return ret;

        }
    }
}
