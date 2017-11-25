package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Table;
import rocks.massi.data.TablesRepository;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.TableNotFoundException;

import java.util.List;

/**
 * Controller for all the table-related operations.
 */
@RestController
@RequestMapping("/v1/tables")
public class TablesController {

    @Autowired
    private TablesRepository tablesRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    /**
     * Get all the tables.
     *
     * @return the tables
     */
    @CrossOrigin
    @GetMapping("/get")
    public List<Table> getTables() {
        return tablesRepository.findAll();
    }

    /**
     * Create new table.
     *
     * @param authorization the authorization token
     * @param table         the table to be added or modified
     * @return the table
     */
    @CrossOrigin(allowedHeaders = {"Authorization", "Content-Type"})
    @PutMapping("/create")
    public Table createTable(@RequestHeader("Authorization") String authorization, @RequestBody Table table) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        return tablesRepository.save(table);
    }

    /**
     * Remove table.
     *
     * @param authorization the authorization
     * @param id            the id
     * @return the table which has been removed
     */
    @CrossOrigin(allowedHeaders = {"Authorization"})
    @DeleteMapping("/remove/{id}")
    public Table removeTable(@RequestHeader("Authorization") String authorization, @PathVariable("id") int id) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        Table toBeRemoved = tablesRepository.findOne(id);

        if (toBeRemoved == null) {
            throw new TableNotFoundException();
        }

        tablesRepository.delete(id);
        return toBeRemoved;
    }
}
