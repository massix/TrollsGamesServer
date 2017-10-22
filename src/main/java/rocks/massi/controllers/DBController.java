package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.connector.SQLiteConnector;

@RestController
@RequestMapping("/v1/dbcontroller")
public class DBController {

    @Autowired
    SQLiteConnector connector;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void createTables() {
        connector.baseSelector.createTableUsers();
        connector.baseSelector.createTableGames();
    }
}
