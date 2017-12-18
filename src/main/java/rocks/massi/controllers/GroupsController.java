package rocks.massi.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import rocks.massi.authentication.Role;
import rocks.massi.authentication.TrollsJwt;
import rocks.massi.data.Group;
import rocks.massi.data.GroupsRepository;
import rocks.massi.data.joins.UsersGroups;
import rocks.massi.data.joins.UsersGroupsRepository;
import rocks.massi.exceptions.AlreadySubscribedToGroup;
import rocks.massi.exceptions.AuthorizationException;
import rocks.massi.exceptions.GroupAlreadyExists;
import rocks.massi.exceptions.GroupDoesNotExist;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * The type Groups controller.
 */
@RestController
@RequestMapping("/v1/groups")
public class GroupsController {

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private UsersGroupsRepository usersGroupsRepository;

    @Autowired
    private TrollsJwt trollsJwt;

    /**
     * Gets all the groups the user has subscribed to + all the open groups.
     *
     * @param authorization the authorization
     * @return all the groups the user can see
     */
    @GetMapping("/get")
    public List<Group> getSubscribedAndOpenGroups(@RequestHeader("Authorization") String authorization) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        List<Group> ret = new LinkedList<>();

        // Find all the groups the user has subscribed to
        List<UsersGroups> groups = usersGroupsRepository.findByUserId(userInformation.getUser());
        groups.forEach(ug -> ret.add(groupsRepository.findOne(ug.getGroupId())));

        // Then find all the public groups
        List<Group> openGroups = groupsRepository.findByStatus(Group.GroupStatus.PUBLIC);

        // Filter out the groups that are already in the list and add them to the result
        openGroups = openGroups.stream().filter(((Predicate<Group>) ret::contains).negate()).collect(Collectors.toList());
        ret.addAll(openGroups);

        // Filter out null elements and return list
        return ret.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Gets all groups.
     *
     * @param authorization the authorization
     * @return the all groups
     */
    @GetMapping("/get/all")
    public List<Group> getAllGroups(@RequestHeader("Authorization") String authorization) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        if (userInformation.getRole() == Role.ADMIN) {
            return groupsRepository.findAll();
        }

        throw new AuthorizationException("User not authorized");
    }

    /**
     * Gets group for id.
     *
     * @param authorization the authorization
     * @param id            the id
     * @return the group for id
     */
    @GetMapping("/get/{id}")
    public Group getGroupForId(@RequestHeader("Authorization") String authorization, @PathVariable("id") Integer id) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        UsersGroups usersGroups = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey(userInformation.getUser(), id));

        if (userInformation.getRole() != Role.ADMIN && usersGroups == null) {
            throw new AuthorizationException("User not authorized");
        }

        Group inDb = groupsRepository.findOne(id);

        if (inDb == null) {
            throw new GroupDoesNotExist("Group with id " + id + " does not exist.");
        }

        return inDb;
    }

    /**
     * Create new group.
     *
     * @param authorization the authorization
     * @param group         the group
     * @return the newly created group
     */
    @PostMapping("/create")
    public Group createNewGroup(@RequestHeader("Authorization") String authorization, @RequestBody Group group) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);

        // Make sure that a group with that name doesn't already exist
        List<Group> inDb = groupsRepository.findByName(group.getName());
        if (!inDb.isEmpty()) {
            throw new GroupAlreadyExists("A group with that name already exists.");
        }

        // Before storing the group nullify the ID so that the underlying DB will create a new one for us
        group.setId(null);

        // Create the actual group
        Group created = groupsRepository.save(group);

        // Create a relation between the user and the newly created group
        UsersGroups relation = new UsersGroups(userInformation.getUser(), created.getId(), UsersGroups.UserRole.ADMINISTRATOR);
        usersGroupsRepository.save(relation);

        return created;
    }

    /**
     * Modify group.
     *
     * @param authorization the authorization
     * @param group         the group
     * @return the modified group
     */
    @PatchMapping("/modify")
    public Group modifyGroup(@RequestHeader("Authorization") String authorization, @RequestBody Group group) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        UsersGroups usersGroups = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey(userInformation.getUser(), group.getId()));

        // Non admin users must be administrators of the group to be able to modify it
        if (userInformation.getRole() != Role.ADMIN && (usersGroups == null || usersGroups.getRole() != UsersGroups.UserRole.ADMINISTRATOR)) {
            throw new AuthorizationException("User not authorized");
        }

        Group inDb = groupsRepository.findOne(group.getId());
        if (inDb == null) {
            throw new GroupDoesNotExist("Group " + group.getName() + " does not exist in DB.");
        }

        return groupsRepository.save(group);
    }

    /**
     * Delete group for id.
     *
     * @param authorization the authorization
     * @param id            the id
     */
    @DeleteMapping("/delete/{id}")
    public void deleteGroupForId(@RequestHeader("Authorization") String authorization, @PathVariable("id") Integer id) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);
        UsersGroups usersGroups = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey(userInformation.getUser(), id));

        if (userInformation.getRole() != Role.ADMIN && (usersGroups == null || usersGroups.getRole() != UsersGroups.UserRole.ADMINISTRATOR)) {
            throw new AuthorizationException("User not authorized");
        }

        Group inDb = groupsRepository.findOne(id);
        if (inDb == null) {
            throw new GroupDoesNotExist("Group " + id + " does not exist in DB.");
        }

        // Cascade delete all the users->group relations
        usersGroupsRepository.deleteByGroupId(id);
        groupsRepository.delete(id);
    }

    /**
     * Subscribes user to group.
     *
     * @param authorization the authorization
     * @param id            the id of the group
     * @return the users->groups relation
     */
    @PostMapping("/subscribe/{id}")
    public UsersGroups subscribeToGroup(@RequestHeader("Authorization") String authorization, @PathVariable("id") Integer id) {
        TrollsJwt.UserInformation userInformation = trollsJwt.getUserInformationFromToken(authorization);

        // Check that the user is not already subscribed to that group
        UsersGroups ug = usersGroupsRepository.findOne(new UsersGroups.UsersGroupsKey(userInformation.getUser(), id));
        if (ug != null) {
            throw new AlreadySubscribedToGroup("User " + userInformation.getUser() + " is already a member of the group.");
        }

        // Check that the group exists
        Group group = groupsRepository.findOne(id);
        if (group == null) {
            throw new GroupDoesNotExist("Group with id " + id + " does not exist");
        }

        // Check that the group is a public one
        if (group.getStatus() != Group.GroupStatus.PUBLIC) {
            throw new AuthorizationException("User not authorized.");
        }

        // An user by default is a member of the group
        return usersGroupsRepository.save(new UsersGroups(userInformation.getUser(), id, UsersGroups.UserRole.MEMBER));
    }
}
