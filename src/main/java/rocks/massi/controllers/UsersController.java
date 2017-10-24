package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.User;
import rocks.massi.utils.DBUtils;

import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UsersController {
    @Autowired
    private DatabaseConnector connector;

    @GetMapping("/get/{nick}")
    public User getUserByNick(@PathVariable("nick") String nick) {
        return getUser(connector, nick);
    }

    @GetMapping("/get")
    public List<User> getAllUsers() {
        return connector.userSelector.getUsers();
    }

    @PostMapping(value = "/add")
    public User addUser(@RequestBody User user) {
        log.info("Got user {}", user.getBggNick());
        connector.userSelector.addUser(user);
        return DBUtils.getUser(connector, user.getBggNick());
    }

    @DeleteMapping("/remove/{nick}")
    public User removeUser(@PathVariable("nick") String nick) {
        val user = DBUtils.getUser(connector, nick);
        if (user != null) {
            connector.userSelector.removeUser(nick);
        }

        return user;
    }
}
