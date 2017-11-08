package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.exceptions.MalformattedUserException;
import rocks.massi.exceptions.UserNotFoundException;
import rocks.massi.utils.DBUtils;

import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UsersController {
    @Autowired
    private UsersRepository usersRepository;

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
        return usersRepository.findAll();
    }

    @PostMapping(value = "/add")
    public User addUser(@RequestBody User user) {
        log.info("Got user {}", user.getBggNick());

        if (user.getBggNick().isEmpty() || user.getForumNick().isEmpty())
            throw new MalformattedUserException("Missing mandatory field");

        usersRepository.save(user);
        return DBUtils.getUser(usersRepository, user.getBggNick());
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
