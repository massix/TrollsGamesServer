package rocks.massi;

import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.User;

import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

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
}
