package rocks.massi.controllers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import rocks.massi.controllers.utils.AuthorizationHandler;
import rocks.massi.data.Group;
import rocks.massi.data.GroupsRepository;
import rocks.massi.data.User;
import rocks.massi.data.UsersRepository;
import rocks.massi.data.joins.UsersGroups;
import rocks.massi.data.joins.UsersGroupsRepository;

import static junit.framework.TestCase.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
public class GroupsControllerTest {

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private UsersGroupsRepository usersGroupsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Before
    public void setUp() {
        // Create some fake groups
        groupsRepository.save(new Group(null, "Trolls de jeux", "Association Antiboise jeux de société", Group.GroupStatus.PUBLIC));
        groupsRepository.save(new Group(null, "JdS06250", "Groupe des joueurs Orange Mougins", Group.GroupStatus.INVITE_ONLY));
        groupsRepository.save(new Group(null, "Twilight Imperialists", "On ne joue qu'à un seul jeu", Group.GroupStatus.CLOSED));


        AuthorizationHandler.setUp(restTemplate);
    }

    @After
    public void tearDown() {
        groupsRepository.deleteAll();
    }

    @Test
    public void getAllGroups() {
        ResponseEntity<Group[]> groups = restTemplate.getForEntity("/v1/groups/get/all", Group[].class);
        assertTrue(groups.getStatusCode().is2xxSuccessful());
        assertEquals(3, groups.getBody().length);
        assertNotSame(groups.getBody()[0].getId(), groups.getBody()[1].getId());
        assertNotSame(groups.getBody()[1].getId(), groups.getBody()[2].getId());
        assertNotSame(groups.getBody()[0].getId(), groups.getBody()[2].getId());
    }

    @Test
    public void createNewGroup() {
        // Group already exists, should receive 409 CONFLICT
        ResponseEntity<Void> conflict = restTemplate.postForEntity("/v1/groups/create", new Group(null, "Trolls de jeux", "Already exists", Group.GroupStatus.CLOSED), Void.class);
        assertEquals(409, conflict.getStatusCodeValue());

        // New group should be created
        ResponseEntity<Group> createdGroup = restTemplate.postForEntity("/v1/groups/create", new Group(null, "New group", "A new group", Group.GroupStatus.PUBLIC), Group.class);
        assertEquals(200, createdGroup.getStatusCodeValue());

        // Get all groups
        ResponseEntity<Group[]> allGroups = restTemplate.getForEntity("/v1/groups/get/all", Group[].class);
        assertEquals(4, allGroups.getBody().length);

        // New group should be the last one
        assertEquals(allGroups.getBody()[3].getName(), "New group");

        // Check that a relation has been created and that the user is the admin of the group
        UsersGroups ug = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey("massi_x", 4L));
        assertNotNull(ug);
        assertEquals(ug.getRole(), UsersGroups.UserRole.ADMINISTRATOR);

        // Forcing ID should not work
        createdGroup = restTemplate.postForEntity("/v1/groups/create", new Group(1L, "Reuse ID", "Description", Group.GroupStatus.PUBLIC), Group.class);
        assertEquals(200, createdGroup.getStatusCodeValue());

        // Get all groups again, we should have one more than before
        allGroups = restTemplate.getForEntity("/v1/groups/get/all", Group[].class);
        assertEquals(5, allGroups.getBody().length);
        assertEquals(allGroups.getBody()[4].getName(), "Reuse ID");
    }

    @Test
    public void modifyGroup() {
        // Group does not exist, should receive 404 NOT FOUND
        Group notExists = restTemplate.patchForObject("/v1/groups/modify", new Group(1024L, "Does not exist", "Dadaumpa", Group.GroupStatus.PUBLIC), Group.class);
        assertNull(notExists);

        // Group does exist, should receive modified group
        Group exists = restTemplate.patchForObject("/v1/groups/modify", new Group(1L, "Trollzzz Jeuxz", "New group", Group.GroupStatus.INVITE_ONLY), Group.class);
        assertNotNull(exists);
        assertEquals(exists.getName(), "Trollzzz Jeuxz");
        assertEquals(exists.getStatus(), Group.GroupStatus.INVITE_ONLY);
    }

    @Test
    public void getGroupForId() {

        // Group with ID does not exist
        ResponseEntity<Void> notExists = restTemplate.getForEntity("/v1/groups/get/1024", Void.class);
        assertEquals(404, notExists.getStatusCodeValue());

        // Group exists
        ResponseEntity<Group> exists = restTemplate.getForEntity("/v1/groups/get/1", Group.class);
        assertEquals(200, exists.getStatusCodeValue());
        assertEquals("Trolls de jeux", exists.getBody().getName());
    }

    @Test
    public void deleteGroupForId() {

        // Group with ID does not exist
        int allGroups = groupsRepository.findAll().size();
        restTemplate.delete("/v1/groups/delete/1024");
        assertEquals(allGroups, groupsRepository.findAll().size());

        // Group exists
        restTemplate.delete("/v1/groups/delete/1");
        assertEquals(allGroups - 1, groupsRepository.findAll().size());
    }

    @Test
    public void getSubscribedAndOpenGroups() {
        // There's only a single open group
        ResponseEntity<Group[]> groups = restTemplate.getForEntity("/v1/groups/get", Group[].class);
        assertEquals(200, groups.getStatusCodeValue());
        assertEquals(1, groups.getBody().length);
        assertEquals("Trolls de jeux", groups.getBody()[0].getName());

        // Subscribe to a private group and check if we now have it as the first element
        usersGroupsRepository.save(new UsersGroups("massi_x", 3L, UsersGroups.UserRole.MEMBER));
        groups = restTemplate.getForEntity("/v1/groups/get", Group[].class);
        assertEquals(200, groups.getStatusCodeValue());
        assertEquals(2, groups.getBody().length);
        assertEquals("Twilight Imperialists", groups.getBody()[0].getName());

        usersGroupsRepository.deleteAll();
    }

    @Test
    public void subscribeToGroup() {
        // Subscribe to a INVITE_ONLY group should fail with 403
        ResponseEntity<Void> subscriptionFail = restTemplate.postForEntity("/v1/groups/subscribe/2", null, Void.class);
        assertEquals(403, subscriptionFail.getStatusCodeValue());

        // Same for CLOSED groups
        subscriptionFail = restTemplate.postForEntity("/v1/groups/subscribe/3", null, Void.class);
        assertEquals(403, subscriptionFail.getStatusCodeValue());

        // Subscription to a non-existing group should fail with 404 NOT FOUND
        subscriptionFail = restTemplate.postForEntity("/v1/groups/subscribe/1024", null, Void.class);
        assertEquals(404, subscriptionFail.getStatusCodeValue());

        // Subscription to an open group should not fail
        ResponseEntity<UsersGroups> subscription = restTemplate.postForEntity("/v1/groups/subscribe/1", null, UsersGroups.class);
        assertEquals(200, subscription.getStatusCodeValue());
        assertEquals("massi_x", subscription.getBody().getUserId());
        assertEquals((Long) 1L, subscription.getBody().getGroupId());

        // Check that the user is now a member of the group
        UsersGroups ug = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey(subscription.getBody().getUserId(), subscription.getBody().getGroupId()));
        assertEquals(UsersGroups.UserRole.MEMBER, ug.getRole());

        // Subsequent subscriptions to the same group should fail with 409 CONFLICT
        subscriptionFail = restTemplate.postForEntity("/v1/groups/subscribe/1", null, Void.class);
        assertEquals(409, subscriptionFail.getStatusCodeValue());

        // Clean DB
        usersGroupsRepository.deleteAll();
    }

    @Test
    public void addMemberToGroup() {
        usersRepository.save(new User("dadaumpa", "Alfred", "alfred@massi.rocks"));

        // Add user to group
        ResponseEntity<UsersGroups> ug = restTemplate.postForEntity("/v1/groups/2/add", new UsersGroups("dadaumpa", 2L, UsersGroups.UserRole.MEMBER), UsersGroups.class);
        assertEquals(200, ug.getStatusCodeValue());
        assertEquals(UsersGroups.UserRole.MEMBER, ug.getBody().getRole());

        // Non existing group in URL should fail with 404 NOT FOUND
        ResponseEntity<Void> fail = restTemplate.postForEntity("/v1/groups/2014/add", new UsersGroups("dadaumpa", 2L, UsersGroups.UserRole.MEMBER), Void.class);
        assertEquals(404, fail.getStatusCodeValue());

        // Mismatch between group in URL and group in object should fail with 409 CONFLICT
        fail = restTemplate.postForEntity("/v1/groups/2/add", new UsersGroups("dadaumpa", 3L, UsersGroups.UserRole.MEMBER), Void.class);
        assertEquals(409, fail.getStatusCodeValue());

        usersRepository.deleteByBggNick("dadaumpa");
    }

    @Test
    public void getMembersForGroup() {
        usersRepository.save(new User("dadaumpa", "Alfred", "alfred@massi.rocks"));
        usersGroupsRepository.save(new UsersGroups("dadaumpa", 1L, UsersGroups.UserRole.MEMBER));
        usersGroupsRepository.save(new UsersGroups("massi_x", 1L, UsersGroups.UserRole.ADMINISTRATOR));

        // 2 members should be part of the group with id 1
        ResponseEntity<UsersGroups[]> users = restTemplate.getForEntity("/v1/groups/1/members", UsersGroups[].class);
        assertEquals(200, users.getStatusCodeValue());
        assertEquals(2, users.getBody().length);

        // Getting members of a non-existing group should throw a 404 NOT FOUND
        ResponseEntity<Void> fail = restTemplate.getForEntity("/v1/groups/1024/members", Void.class);
        assertEquals(404, fail.getStatusCodeValue());

        usersGroupsRepository.deleteAll();
        usersRepository.deleteByBggNick("dadaumpa");
    }

    @Test
    public void unsubscribeFromGroup() {
        usersGroupsRepository.save(new UsersGroups("massi_x", 1L, UsersGroups.UserRole.MEMBER));
        ResponseEntity<Void> delete = restTemplate.exchange("/v1/groups/unsubscribe/2", HttpMethod.DELETE, null, Void.class);
        assertEquals(409, delete.getStatusCodeValue());

        delete = restTemplate.exchange("/v1/groups/unsubscribe/1", HttpMethod.DELETE, null, Void.class);
        assertEquals(200, delete.getStatusCodeValue());
        assertNull(usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey("massi_x", 1L)));
    }

    @Test
    public void removeMemberFromGroup() {
        usersRepository.save(new User("dadaumpa", "Alfred", "alfred@massi.rocks"));
        usersGroupsRepository.save(new UsersGroups("dadaumpa", 2L, UsersGroups.UserRole.MEMBER));

        ResponseEntity<Void> delete = restTemplate.exchange("/v1/groups/1/remove/dadaumpa", HttpMethod.DELETE, null, Void.class);
        assertEquals(404, delete.getStatusCodeValue());

        delete = restTemplate.exchange("/v1/groups/2/remove/dadaumpa", HttpMethod.DELETE, null, Void.class);
        assertEquals(200, delete.getStatusCodeValue());
        assertNull(usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey("dadaumpa", 2L)));
    }
}