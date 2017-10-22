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

    public DatabaseConnector(@Value("${db.fileLocation}") final String fileLocation,
                             @Value("${db.user}") final String user,
                             @Value("${db.password") final String password) {
        String dbUrl = fileLocation + "?user=" + user + "&password=" + password;
        String herokuBase = System.getenv("JDBC_DATABASE_URL");

        if (herokuBase != null) {
            dbi = new DBI(herokuBase);
        }
        else dbi = new DBI(dbUrl);
        gameSelector = dbi.open(GameSelector.class);
        userSelector = dbi.open(UserSelector.class);
        baseSelector = dbi.open(BaseSelector.class);
    }
}
