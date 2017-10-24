package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import rocks.massi.connector.DatabaseConnector;

@RestController
@RequestMapping("/v1/dbcontroller")
public class DBController {

    @Autowired
    DatabaseConnector connector;

    @PostMapping("/create")
    public void createTables() {
        connector.baseSelector.createTableUsers();
        connector.baseSelector.createTableGames();
    }
}
