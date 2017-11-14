package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.LoginInformation;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.exceptions.AuthenticationException;
import rocks.massi.exceptions.MalformattedUserException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private JavaMailSender mailSender;

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

    @CrossOrigin
    @GetMapping(value = "/confirm")
    public User confirmRegistration(@RequestParam("email") final String email, @RequestParam("token") final String token) {
        User ret = trollsJwt.confirmRegistrationTokenForEmail(email, token);

        if (ret == null) {
            throw new AuthenticationException("Can't go any further.");
        }

        ret.setRole(Role.USER);
        ret.setAuthenticationType(AuthenticationType.JWT);

        usersRepository.save(ret);
        return usersRepository.findByEmail(email);
    }

    @CrossOrigin
    @PostMapping(value = "/register")
    public User registerNewUser(@RequestBody User user) {
        // Check that the user exists on bgg

        // Check that the user doesn't already exist
        if (usersRepository.findByEmail(user.getEmail()) != null || usersRepository.findByBggNick(user.getBggNick()) != null) {
            throw new AuthenticationException("User already exist. Please ask for a password reset.");
        }

        // New users are by default unable to login to the server.
        user.setAuthenticationType(AuthenticationType.NONE);
        user.setRole(Role.USER);

        // Encrypt password
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        // Generate a OT token for user
        String host = System.getenv("VIRTUAL_HOST");
        String token = trollsJwt.generateRegistrationTokenForUser(user);

        // Send email asking user to verify their email address
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("Trolls De Jeux <noreply@massi.rocks>");
        mailMessage.setReplyTo("massi@massi.rocks");
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Verify your subscription to the service!");
        mailMessage.setText("Confirmez votre blabla @ https://" + host + "/v1/users/confirm?email=" + user.getEmail() + "&token=" + token);
        mailSender.send(mailMessage);

        return user;
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

    @CrossOrigin(allowedHeaders = {"Authorization"})
    @PostMapping(value = "/login")
    public User login(@RequestBody LoginInformation loginInformation, HttpServletResponse servletResponse) {
        log.info("Requested login for user {}", loginInformation.getEmail());

        // Login will be forced using email, so we won't use the DBUtils!
        User dbUser = usersRepository.findByEmail(loginInformation.getEmail());

        if (dbUser == null) {
            log.error("User {} does not exist", loginInformation.getEmail());
            throw new UserNotFoundException("User doesn't exist in database.");
        }

        if (dbUser.getAuthenticationType() == AuthenticationType.NONE) {
            log.error("User {} authType = NONE", dbUser.getEmail());
            throw new AuthenticationException("User authentication type is none, missing registration.");
        }

        try {
            if (BCrypt.checkpw(loginInformation.getPassword(), dbUser.getPassword())) {
                String token = trollsJwt.getTokenForUser(dbUser);
                if (token.isEmpty()) {
                    token = trollsJwt.generateNewTokenForUser(dbUser);
                }

                dbUser.setPassword("*");
                servletResponse.setHeader("Authorization", String.format("Bearer %s", token));
                return dbUser;
            }
        } catch (IllegalArgumentException exception) {
            log.error("User {} failed to login because: {}", loginInformation.getEmail(), exception.getMessage());
        }

        throw new AuthenticationException("Username or password are WRONG");
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
