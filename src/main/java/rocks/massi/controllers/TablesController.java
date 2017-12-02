package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.TableEntity;
import rocks.massi.data.TablesRepository;
import rocks.massi.data.joins.EventTablesRepository;
import rocks.massi.data.joins.TableGamesRepository;
import rocks.massi.data.joins.TableUsersRepository;
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
    private TableUsersRepository tableUsersRepository;

    @Autowired
    private TableGamesRepository tableGamesRepository;

    @Autowired
    private EventTablesRepository eventTablesRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    /**
     * Get all the tables.
     *
     * @return the tables
     */
    @CrossOrigin
    @GetMapping("/get")
    public List<TableEntity> getTables() {
        return tablesRepository.findAll();
    }

    /**
     * Gets table.
     *
     * @param tableId the table id
     * @return the table
     */
    @CrossOrigin
    @GetMapping("/get/{id}")
    public TableEntity getTable(@PathVariable("id") int tableId) {
        TableEntity ret = tablesRepository.findOne(tableId);
        if (ret == null) {
            throw new TableNotFoundException();
        }

        return ret;
    }

    /**
     * Create new table.
     *
     * @param authorization the authorization token
     * @param tableEntity   the table to be added or modified
     * @return the table
     */
    @CrossOrigin(allowedHeaders = {"Authorization", "Content-Type"})
    @PutMapping("/create")
    public TableEntity createTable(@RequestHeader("Authorization") String authorization, @RequestBody TableEntity tableEntity) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        int newId = 0;

        // Calculates the highest id
        for (TableEntity t : tablesRepository.findAll()) {
            if (t.getId() >= newId) {
                newId = t.getId() + 1;
            }
        }

        tableEntity.setId(newId);
        return tablesRepository.save(tableEntity);
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
    public TableEntity removeTable(@RequestHeader("Authorization") String authorization, @PathVariable("id") int id) {
        if (trollsJwt.getUserInformationFromToken(authorization).getRole() != Role.ADMIN) {
            throw new AuthorizationException("User not authorized.");
        }

        TableEntity toBeRemoved = tablesRepository.findOne(id);

        if (toBeRemoved == null) {
            throw new TableNotFoundException();
        }

        // Remove also the references in other repositories
        eventTablesRepository.deleteByTableId(id);
        tableUsersRepository.deleteByTableId(id);
        tableGamesRepository.deleteByTableId(id);

        tablesRepository.delete(id);
        return toBeRemoved;
    }
}
