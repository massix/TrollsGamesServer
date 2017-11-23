package rocks.massi.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Quote;
import rocks.massi.data.QuotesRepository;
import rocks.massi.exceptions.AuthorizationException;

import java.util.List;
import java.util.Random;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/quotes")
public class QuotesController {

    @Autowired
    private final QuotesRepository quotesRepository;

    @Autowired
    private final TrollsJwt trollsJwt;

    @CrossOrigin
    @GetMapping("/get/random")
    public Quote getRandomQuote() {
        List<Quote> quotes = quotesRepository.findAll();
        return quotes.get(new Random().nextInt(quotes.size()));
    }

    @CrossOrigin(allowedHeaders = {"Authorization", "Content-Type"})
    @PutMapping("/add")
    public Quote addQuote(@RequestHeader("Authorization") String authorization, @RequestBody Quote quote) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (userInformation.getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        quotesRepository.save(quote);
        return quote;
    }

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/remove")
    public Quote removeQuote(@RequestHeader("Authorization") String authorization, @Param("quote") String quote) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        Quote ret = quotesRepository.findByQuote(quote);
        quotesRepository.deleteByQuote(quote);
        return ret;
    }

    @CrossOrigin
    @GetMapping("/get")
    public List<Quote> getAllQuotes() {
        return quotesRepository.findAll();
    }
}
