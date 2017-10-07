package rocks.massi.connector;

import lombok.val;
import org.junit.Assert;
import org.junit.Test;
import rocks.massi.data.Game;
import rocks.massi.data.User;

import java.util.LinkedList;
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

    @Test
    public void testAddGame() {
        val g = new Game(
                666,
                "Cyclades 2",
                "New Version of Cyclades!",
                1,
                2,
                40,
                2008,
                666,
                false,
                "",
                "",
                "");

        connector.gameSelector.insertGame(g);
        Assert.assertEquals("Game does not match", connector.gameSelector.findById(666).getName(), "Cyclades 2");
        connector.gameSelector.removeGameById(666);
    }

    @Test
    public void testRemoveGame() {
        val g = new Game(
                666,
                "Cyclades 2",
                "New Version of Cyclades!",
                1,
                2,
                40,
                2008,
                666,
                false,
                "",
                "",
                "");

        connector.gameSelector.insertGame(g);
        connector.gameSelector.removeGame(g);

        Assert.assertNull(connector.gameSelector.findById(666));
    }

    @Test
    public void getUserCollection() {
        val u = connector.userSelector.findByBggNick("massi_x");
        u.buildCollection();

        Assert.assertFalse(u.getCollection().isEmpty());
        LinkedList<Game> gamesInCollection = new LinkedList<>();

        for (val i : u.getCollection()) {
            gamesInCollection.add(connector.gameSelector.findById(i));
        }

        Assert.assertFalse(gamesInCollection.isEmpty());
    }
}