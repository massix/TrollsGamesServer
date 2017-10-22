package rocks.massi.connector;

import lombok.val;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rocks.massi.data.Game;
import rocks.massi.data.User;

import java.util.LinkedList;
import java.util.List;

public class SQLiteConnectorTest {
    private final SQLiteConnector connector = new SQLiteConnector("jdbc:postgresql://localhost/trolls_test", "postgres", "");

    @Before
    public void testCreateTables() {
        connector.baseSelector.createTableGames();
        connector.baseSelector.createTableUsers();
        connector.baseSelector.truncateTableGames();
        connector.baseSelector.truncateTableUsers();

        // Insert some games
        connector.gameSelector.insertGame(new Game(54998, "Cyclades", "", 2, 18, 120, 2001, 1, false, "", "", ""));

        // Insert some users
        connector.userSelector.addUser(new User("massi_x", "massi_x", "54998", "54998"));
    }

    @After
    public void dropTables() {
        connector.baseSelector.dropTableGames();
        connector.baseSelector.dropTableUsers();
    }

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
        User k = connector.userSelector.findByForumNick("massi_x");
        Assert.assertNotNull(u);
        Assert.assertNotNull(k);
        Assert.assertTrue(u.getForumNick().equals("massi_x"));
        Assert.assertEquals("Users are not the same?", u, k);
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
    public void testRemoveUser() {
        val u = new User("massi_x", "massi_x", "", "");
        connector.userSelector.removeUser("massi_x");

        Assert.assertNull(connector.userSelector.findByBggNick("massi_x"));
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