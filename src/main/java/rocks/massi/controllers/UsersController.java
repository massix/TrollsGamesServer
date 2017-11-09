package rocks.massi.controllers;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.exceptions.MalformattedUserException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.exceptions.WrongAuthenticationException;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UsersController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @CrossOrigin
    @GetMapping("/get/{nick}")
    public User getUserByNick(@PathVariable("nick") String nick) {
        User user = getUser(usersRepository, nick);

        if (user == null) {
            log.error("User {} not found", nick);
            throw new UserNotFoundException(String.format("User %s not found on server", nick));
        }

        return user;
    }

    @CrossOrigin
    @GetMapping("/get")
    public List<User> getAllUsers() {
        LinkedList<User> ret = new LinkedList<>();

        // Do not send passwords back.
        usersRepository.findAll().forEach(user -> {
            user.setPassword("*");
            ret.add(user);
        });

        return ret;
    }

    @PostMapping(value = "/add")
    public User addUser(@RequestBody User user) {
        log.info("Got user {}", user.getBggNick());

        // Users added via APIs are by default of role user and authenticated via JWT
        user.setAuthenticationType(AuthenticationType.JWT);
        user.setRole(Role.USER);

        if (user.getBggNick().isEmpty() || user.getForumNick().isEmpty() || user.getPassword().isEmpty())
            throw new MalformattedUserException("Missing mandatory field");

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
        return DBUtils.getUser(usersRepository, user.getBggNick());
    }

    @PostMapping(value = "/login")
    public User login(@RequestBody User user, HttpServletResponse servletResponse) {
        log.info("Requested login for user {}", user.getBggNick());

        // Login will be forced using BGGNick, so we won't use the DBUtils!
        User dbUser = usersRepository.findByBggNick(user.getBggNick());

        if (dbUser == null) {
            throw new UserNotFoundException("User doesn't exist in database.");
        }

        if (BCrypt.checkpw(user.getPassword(), dbUser.getPassword())) {
            String token = Jwts.builder()
                    .setSubject(dbUser.getBggNick())
                    .setExpiration(new Date(System.currentTimeMillis() + 640_000_000L))
                    .signWith(SignatureAlgorithm.HS512, "test")
                    .compact();
            dbUser.setPassword("*");
            servletResponse.setHeader("Authentication", String.format("Bearer %s", token));
            return dbUser;
        }

        throw new WrongAuthenticationException("Username or password are WRONG");
    }

    @DeleteMapping("/remove/{nick}")
    public User removeUser(@PathVariable("nick") String nick) {
        val user = DBUtils.getUser(usersRepository, nick);
        if (user != null) {
            usersRepository.deleteByBggNick(nick);
        }
        else {
            throw new UserNotFoundException(String.format("User %s not found on server.", nick));
        }

        return user;
    }
}
