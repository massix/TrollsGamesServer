package rocks.massi.connector;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import rocks.massi.data.Game;
import rocks.massi.data.User;

import javax.jws.soap.SOAPBinding;
import java.util.List;

public class SQLiteConnectorTest {
    private final SQLiteConnector connector = new SQLiteConnector();

    @Test
    public void testGetAllGames() {
        List<Game> games = connector.gameSelector.getGames();
        Assert.assertFalse(games.isEmpty());
    }

    @Test
    public void testGetGame() {
        Game g = connector.gameSelector.findById(54998);
        Assert.assertNotNull(g);
        Assert.assertEquals(
                "Was expecting Cyclades, got something else.",
                g.getName(),
                "Cyclades");
    }

    @Test
    public void testGetUser() {
        User u = connector.userSelector.findByBggNick("massi_x");
        Assert.assertTrue(u.getForumNick().equals("massi_x"));
    }

    @Test
    public void testGetAllUsers() {
        List<User> users = connector.userSelector.getUsers();
        Assert.assertFalse(users.isEmpty());
    }
}