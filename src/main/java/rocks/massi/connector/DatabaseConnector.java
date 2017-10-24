package rocks.massi.connector;

import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnector {
    public GameSelector gameSelector;
    public UserSelector userSelector;
    public BaseSelector baseSelector;
    private DBI dbi;

    public DatabaseConnector(@Value("${db.url}") final String dbUrl) {
        String herokuBase = System.getenv("JDBC_DATABASE_URL");

        dbi = new DBI(dbUrl);
        gameSelector = dbi.open(GameSelector.class);
        userSelector = dbi.open(UserSelector.class);
        baseSelector = dbi.open(BaseSelector.class);
    }
}
