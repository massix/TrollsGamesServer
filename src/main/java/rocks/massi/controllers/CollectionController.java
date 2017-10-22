package rocks.massi.controllers;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.connector.SQLiteConnector;
import rocks.massi.data.Game;

import java.util.LinkedList;
import java.util.List;

import static rocks.massi.utils.DBUtils.getUser;

@Slf4j
@RestController
@RequestMapping("/v1/collection")
public class CollectionController {
    @Autowired
    private SQLiteConnector connector;

    @RequestMapping("/get/{nick}")
    public List<Game> getCollection(@PathVariable("nick") final String nick) {
        val user = getUser(connector, nick);
        LinkedList<Game> collection = new LinkedList<>();

        if (user != null) {
            user.buildCollection();
            user.getCollection().forEach(id -> collection.add(connector.gameSelector.findById(id)));
        }

        return collection;
    }
}
