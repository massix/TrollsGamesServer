package rocks.massi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import rocks.massi.data.Stats;
import rocks.massi.data.StatsRepository;

import java.security.MessageDigest;
import java.util.List;

@Slf4j
@Component
public class StatsLogger {

    @Autowired
    private StatsRepository statsRepository;

    public final void logStat(String endpoint, String userAgent) {
        try {
            String md5 = new String(Hex.encode(MessageDigest.getInstance("MD5").digest(userAgent.getBytes("UTF-8"))));
            Stats stats = statsRepository.findOne(new Stats.StatsKey(md5, endpoint));

            if (stats == null) {
                stats = new Stats(md5, endpoint, 1);
            } else {
                stats = new Stats(stats.getHashedUser(), stats.getEndpoint(), stats.getCounter() + 1);
            }

            statsRepository.save(stats);
        } catch (Exception e) {
            log.error("Could not generate statistic.");
        }
    }

    public final List<Stats> getAllStats() {
        return statsRepository.findAllByOrderByHashedUser();
    }
}

