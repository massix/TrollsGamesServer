package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.User;
import rocks.massi.utils.DBUtils;
import sun.misc.Request;

import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
public class UsersController {
    @Autowired
    private SQLiteConnector connector;

    @RequestMapping("/v1/users/get/{nick}")
    public User getUserByNick(@PathVariable("nick") String nick) {
        return getUser(connector, nick);
    }

    @RequestMapping("/v1/users/get")
    public List<User> getAllUsers() {
        return connector.userSelector.getUsers();
    }

    @RequestMapping(value = "/v1/users/add", method = RequestMethod.POST)
    public User addUser(@RequestBody User user) {
        log.info("Got user {}", user.getBggNick());
        connector.userSelector.addUser(user);
        return DBUtils.getUser(connector, user.getBggNick());
    }

    @RequestMapping(value = "/v1/users/del/{nick}", method = RequestMethod.DELETE)
    public User removeUser(@PathVariable("nick") String nick) {
        val user = DBUtils.getUser(connector, nick);
        if (user != null) {
            connector.userSelector.removeUser(nick);
        }

        return user;
    }
}
