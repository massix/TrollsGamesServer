package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.AuthenticationType;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.crawler.CollectionCrawler;
import rocks.massi.data.LoginInformation;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.OwnershipsRepository;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.MalformattedUserException;
import rocks.massi.exceptions.UserAlreadyExistsException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.DBUtils;

import javax.servlet.http.HttpServletResponse;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@CrossOrigin(allowedHeaders = {"Authorization", "Content-Type"})
@RequestMapping("/v1/users")
public class UsersController {

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private OwnershipsRepository ownershipsRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private CollectionCrawler collectionCrawler;

    @Autowired
    private TrollsJwt trollsJwt;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${users-controller.default-role}")
    private Role defaultRole;

    @GetMapping("/get/{nick}")
    public User getUserByNick(@PathVariable("nick") String nick) {
        User user = getUser(usersRepository, nick);

        if (user == null) {
            log.error("User {} not found", nick);
            throw new UserNotFoundException(String.format("User %s not found on server", nick));
        }

        return user;
    }

    @GetMapping("/get/{nick}/information")
    public TrollsJwt.UserInformation getUserInformation(@RequestHeader("Authorization") String authorization,
                                                        @PathVariable("nick") String user) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);

        if (!userInformation.getUser().equals(user) && userInformation.getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        return userInformation;
    }

    @GetMapping("/get")
    public List<User> getAllUsers() {
        LinkedList<User> ret = new LinkedList<>();

        // Do not send passwords back.
        usersRepository.findByAuthenticationTypeNot(AuthenticationType.NONE).forEach(user -> {
            user.setPassword("*");
            ret.add(user);
        });

        return ret;
    }

    @GetMapping("/search")
    public List<User> searchForUser(@Param("query") String query) {
        List<User> users = usersRepository.findAll();
        List<User> result = new LinkedList<>();

        List<String> userNames = new LinkedList<>();
        users.forEach(u -> userNames.add(u.getBggNick()));

        FuzzySearch.extractAll(query, userNames, 75).forEach(username -> {
            result.add(users.stream().filter(u -> u.getBggNick().equals(username.getString())).collect(Collectors.toList()).get(0));
        });

        return result;
    }

    @GetMapping(value = "/confirm")
    public User confirmRegistration(@RequestParam("email") final String email,
                                    @RequestParam("token") final String token,
                                    @Param("redirect") final String redirect,
                                    HttpServletResponse servletResponse) {
        User ret = usersRepository.findByEmail(email);

        if (!trollsJwt.confirmRegistrationTokenForEmail(email, token) || ret == null) {
            throw new AuthorizationException("Can't go any further.");
        }

        ret.setRole(Role.USER);
        ret.setAuthenticationType(AuthenticationType.JWT);

        usersRepository.save(ret);

        if (!StringUtils.isEmpty(redirect)) {
            servletResponse.setHeader("Location", redirect);
            servletResponse.setStatus(HttpServletResponse.SC_FOUND);
        }

        return usersRepository.findByEmail(email);
    }

    @PostMapping(value = "/register")
    public User registerNewUser(@RequestBody User user,
                                @Param("redirect") String redirect,
                                HttpServletResponse servletResponse) {

        // Check that the user doesn't already exist
        if (usersRepository.findByEmail(user.getEmail()) != null || usersRepository.findByBggNick(user.getBggNick()) != null) {
            throw new AuthorizationException("User already exist. Please ask for a password reset.");
        }

        // New users are by default unable to login to the server.
        user.setAuthenticationType(AuthenticationType.NONE);
        user.setRole(defaultRole);

        // Encrypt password
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        // Save the user in the DB
        usersRepository.save(user);

        // Check that the user exists on bgg, if it exists, start crawling it right now
        if (user.isBggHandled() && !collectionCrawler.checkUserExists(user)) {
            throw new UserNotFoundException("User not found on BGG.");
        }


        // Generate a OT token for user
        String token = trollsJwt.generateRegistrationTokenForUser(user);

        servletResponse.addHeader("X-Token-Validation", token);

        String host = System.getenv("VIRTUAL_HOST");
        // Send email asking user to verify their email address
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("Trolls De Jeux <noreply@massi.rocks>");
        mailMessage.setReplyTo("massi@massi.rocks");
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Verify your subscription to the service!");
        mailMessage.setText("Bienvenue à bord, " + user.getForumNick() + "!\n" +
                "Veuillez confirmer votre nouveau compte en cliquant sur le lien suivant!\nhttps://" +
                host + "/v1/users/confirm?email=" + user.getEmail() + "&token=" + token +
                (redirect != null ? "&redirect=" + redirect : ""));
        mailSender.send(mailMessage);

        return user;
    }

    @PostMapping(value = "/add")
    public User addUser(@RequestHeader("Authorization") String authorization, @RequestBody User user) {
        log.info("Got user {}", user.getBggNick());

        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (userInformation.getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        if (getUser(usersRepository, user.getBggNick()) != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // Users added via APIs are by default of role user and authenticated via JWT
        user.setAuthenticationType(AuthenticationType.JWT);
        user.setRole(defaultRole);

        if (user.getBggNick().isEmpty() || user.getForumNick().isEmpty() || user.getPassword().isEmpty())
            throw new MalformattedUserException("Missing mandatory field");

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        usersRepository.save(user);
        return DBUtils.getUser(usersRepository, user.getBggNick());
    }

    @PatchMapping("/modify")
    public User modifyUser(@RequestHeader("Authorization") String authorization, @RequestBody User user) {
        log.info("Modifying user {}", user.getBggNick());

        // Check that the user is either himself or an admin
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (userInformation.getUser().equals(user.getBggNick()) || userInformation.getRole() == Role.ADMIN) {
            User oldUser = getUser(usersRepository, user.getBggNick());
            if (oldUser == null) {
                throw new UserNotFoundException("User not found. Please use the /add endpoint or /register");
            }

            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

            // Force JWT because we.. well, that's what we do.
            user.setAuthenticationType(AuthenticationType.JWT);

            usersRepository.save(user);
            return usersRepository.findByBggNick(user.getBggNick());
        }

        throw new AuthorizationException("User not authorized.");
    }

    @CrossOrigin(exposedHeaders = {"Authorization"})
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
            throw new AuthorizationException("User authentication type is none, missing registration.");
        }

        try {
            if (BCrypt.checkpw(loginInformation.getPassword(), dbUser.getPassword())) {
                String token = trollsJwt.generateOrRetrieveTokenForUser(dbUser);
                dbUser.setPassword("*");
                servletResponse.setHeader("Authorization", String.format("Bearer %s", token));
                return dbUser;
            }
        } catch (IllegalArgumentException exception) {
            log.error("User {} failed to login because: {}", loginInformation.getEmail(), exception.getMessage());
        }

        throw new AuthorizationException("Username or password are WRONG");
    }

    @DeleteMapping("/remove/{nick}")
    public User removeUser(@RequestHeader("Authorization") final String authorization,
                           @PathVariable("nick") String nick) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        val user = DBUtils.getUser(usersRepository, nick);
        if (user != null) {
            ownershipsRepository.deleteByUser(nick);
            usersRepository.deleteByBggNick(nick);
        }
        else {
            throw new UserNotFoundException(String.format("User %s not found on server.", nick));
        }

        return user;
    }
}
