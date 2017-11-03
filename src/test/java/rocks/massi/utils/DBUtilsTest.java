package rocks.massi.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.connector.DatabaseConnector;
import rocks.massi.data.User;

import static junit.framework.TestCase.*;

@ActiveProfiles("local")
@RunWith(SpringRunner.class)
@SpringBootTest
public class DBUtilsTest {
    @Autowired
    private DatabaseConnector databaseConnector;

    @Before
    public void setUp() throws Exception {
        databaseConnector.baseSelector.dropTableUsers();
        databaseConnector.baseSelector.dropTableGames();

        databaseConnector.baseSelector.createTableUsers();
        databaseConnector.baseSelector.createTableGames();

        databaseConnector.userSelector.addUser(new User("bgg_nick", "forum_nick", "", ""));
        databaseConnector.userSelector.addUser(new User("bgg_nick_2", "forum_nick_2", "", ""));
    }

    @After
    public void tearDown() throws Exception {
        databaseConnector.baseSelector.dropTableGames();
        databaseConnector.baseSelector.dropTableUsers();
    }

    @Test
    public void getUser() throws Exception {
        User user = DBUtils.getUser(databaseConnector, "bgg_nick");
        assertNotNull(user);
        assertEquals("forum_nick", user.getForumNick());
        assertEquals("bgg_nick", user.getBggNick());

        user = DBUtils.getUser(databaseConnector, "bgg_nick_2");
        assertNotNull(user);
        assertEquals("forum_nick_2", user.getForumNick());
        assertEquals("bgg_nick_2", user.getBggNick());

        assertNull(DBUtils.getUser(databaseConnector, "non_existing"));
    }

}