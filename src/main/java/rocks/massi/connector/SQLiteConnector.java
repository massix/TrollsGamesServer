package rocks.massi.connector;

import lombok.extern.log4j.Log4j;
import org.skife.jdbi.v2.DBI;

@Log4j
public class SQLiteConnector {
    public GameSelector gameSelector;
    public UserSelector userSelector;
    private DBI dbi;

    public SQLiteConnector() {
        dbi = new DBI("jdbc:sqlite:trolls.db");
        gameSelector = dbi.open(GameSelector.class);
        userSelector = dbi.open(UserSelector.class);
    }
}
