package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.cache.CrawlCache;
import rocks.massi.data.CacheOperation;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/cache")
public class CacheController {

    @Autowired
    private CrawlCache crawlCache;

    @DeleteMapping("/purge")
    public CacheOperation purgeCache() {
        try {
            crawlCache.purgeAll();
            return new CacheOperation(true, "", new LinkedList<>());
        }
        catch (IOException exception) {
            return new CacheOperation(false, exception.getMessage(), new LinkedList<>());
        }
    }

    @DeleteMapping("/expired")
    public CacheOperation purgeExpired() {
        try {
            crawlCache.purgeExpired();
            return new CacheOperation(true, "", new LinkedList<>());
        }
        catch (IOException exception) {
            return new CacheOperation(false, exception.getMessage(), new LinkedList<>());
        }
    }

    @GetMapping("/get")
    public CacheOperation getMemoryCache() {
        List<CacheOperation.CacheEntry> entries = new LinkedList<>();
        crawlCache.getCache().forEach((k, v) -> entries.add(new CacheOperation.CacheEntry(k, v, new Date(v * 1000).toString())));
        return new CacheOperation(true, "", entries);
    }
}
