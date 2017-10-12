package rocks.massi.connector;

import org.skife.jdbi.v2.DBI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SQLiteConnector {
    public GameSelector gameSelector;
    public UserSelector userSelector;
    public BaseSelector baseSelector;
    private DBI dbi;

    public SQLiteConnector(@Value("${db.fileLocation}") final String fileLocation) {
        dbi = new DBI(fileLocation);
        gameSelector = dbi.open(GameSelector.class);
        userSelector = dbi.open(UserSelector.class);
        baseSelector = dbi.open(BaseSelector.class);
    }
}
