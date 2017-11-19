package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Quote;
import rocks.massi.data.QuotesRepository;
import rocks.massi.data.ServerInformation;
import rocks.massi.data.Stats;
import rocks.massi.exceptions.AuthenticationException;
import rocks.massi.utils.StatsLogger;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Random;

@RestController
@RequestMapping("/v1/server")
public class ServerController {

    @Autowired
    private QuotesRepository quotesRepository;

    @Autowired
    private StatsLogger statsLogger;

    @Autowired
    private TrollsJwt trollsJwt;

    @CrossOrigin
    @GetMapping("/information")
    public ServerInformation getServerInformation(@RequestHeader("User-Agent") final String userAgent) throws IOException {
        final Properties properties = new Properties();
        properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        statsLogger.logStat("server/information", userAgent);
        return new ServerInformation(
                properties.getProperty("server_version"),
                properties.getProperty("artifact_name"),
                properties.getProperty("build_time"));
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @GetMapping("/stats")
    public List<Stats> getStats(@RequestHeader("Authorization") final String authorization) {
        if (!trollsJwt.checkHeaderWithToken(authorization)) {
            throw new AuthenticationException("User not authorized");
        }

        return statsLogger.getAllStats();
    }

    @CrossOrigin
    @GetMapping("/quote")
    public Quote getQuote() throws IOException {
        List<Quote> quotes = quotesRepository.findAll();
        return quotes.get(new Random().nextInt(quotes.size()));
    }
}
