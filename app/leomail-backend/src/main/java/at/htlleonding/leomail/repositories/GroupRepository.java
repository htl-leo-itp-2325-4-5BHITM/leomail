package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.entities.*;
import at.htlleonding.leomail.model.dto.contacts.CompanyContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import at.htlleonding.leomail.model.dto.groups.GroupDetailDTO;
import at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO;
import at.htlleonding.leomail.model.exceptions.projects.ProjectNotExistsException;
import at.htlleonding.leomail.services.KeycloakAdminService;
import at.htlleonding.leomail.services.PermissionService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupRepository {

    private static final Logger LOGGER = Logger.getLogger(GroupRepository.class);

    @Inject
    EntityManager em;

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    PermissionService permissionService;

    public List<GroupOverviewDTO> getPersonalGroups(String projectId, String accountId) {
        validateProjectAndAccount(projectId, accountId);

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupOverviewDTO(g.id, g.name, CONCAT(c.firstName, ' ', c.lastName)) " +
                                "FROM Group g JOIN g.createdBy c WHERE g.project.id = :projectId AND g.createdBy.id = :accountId",
                        GroupOverviewDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("accountId", accountId)
                .getResultList();
    }

    public GroupDetailDTO getGroupDetails(String projectId, String accountId, String groupId) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupExistence(projectId, groupId);

        Group group = Group.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group not found");
        }

        CreatorDTO createdBy = new CreatorDTO(
                group.createdBy.id,
                group.createdBy instanceof NaturalContact naturalContact ? naturalContact.firstName : "",
                group.createdBy instanceof NaturalContact naturalContact ? naturalContact.lastName : "",
                group.createdBy instanceof NaturalContact naturalContact ? naturalContact.mailAddress : ""
        );

        List<ContactSearchResult> members = group.members.stream()
                .map(contact -> {
                    if (contact instanceof NaturalContact naturalContact) {
                        return new NaturalContactSearchDTO(
                                naturalContact.id,
                                naturalContact.firstName,
                                naturalContact.lastName,
                                naturalContact.mailAddress
                        );
                    } else if (contact instanceof CompanyContact companyContact) {
                        return new CompanyContactSearchDTO(
                                companyContact.id,
                                companyContact.companyName,
                                companyContact.mailAddress
                        );
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new GroupDetailDTO(
                group.id,
                group.name,
                group.description,
                createdBy,
                members
        );
    }

    @Transactional
    public void createGroup(String projectId, String accountId, String description, String name, List<?> members) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupName(name);
        validateUniqueGroupName(projectId, name);

        if (members == null) members = List.of();

        validateMembersContactType(members);

        HashSet<Contact> memberList = fetchExistingContacts(members);
        addKeycloakUsers(memberList, members);

        Group newGroup = new Group(name, description, Contact.findById(accountId), Project.findById(projectId), memberList);
        em.persist(newGroup);
        LOGGER.infof("Group '%s' successfully created.", name);
    }

    @Transactional
    public void deleteGroup(String projectId, String accountId, String groupId) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupId(groupId);

        int deletedCount = em.createQuery("DELETE FROM Group g WHERE g.id = :groupId")
                .setParameter("groupId", groupId)
                .executeUpdate();

        if (deletedCount > 0) {
            LOGGER.infof("Group with ID %s successfully deleted.", groupId);
        } else {
            LOGGER.warnf("Attempted to delete non-existent group with ID %s.", groupId);
            throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
        }
    }

    @Transactional
    public void updateGroup(String projectId, String accountId, String description, String groupId, String name, List<?> members) {
        validateProjectAndAccount(projectId, accountId);
        validateGroupId(groupId);
        validateGroupName(name);
        validateUniqueGroupNameForUpdate(projectId, groupId, name);

        if (members == null) members = List.of();

        validateMembersContactType(members);

        Group group = Group.findById(groupId);
        if (group == null) {
            throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");
        }

        group.name = name;
        group.description = description;

        HashSet<Contact> memberList = fetchExistingContacts(members);
        addKeycloakUsers(memberList, members);
        group.members = memberList;

        LOGGER.infof("Group with ID %s successfully updated.", groupId);
    }

    public List<GroupDetailDTO> searchGroups(String searchTerm, String projectId, String userId) {
        validateProjectAndAccount(projectId, userId);

        return em.createQuery(
                        "SELECT NEW at.htlleonding.leomail.model.dto.groups.GroupDetailDTO(g.id, g.name, g.description, null, null) " +
                                "FROM Group g WHERE g.project.id = :projectId AND g.name LIKE :searchTerm",
                        GroupDetailDTO.class)
                .setParameter("projectId", projectId)
                .setParameter("searchTerm", "%" + searchTerm + "%")
                .getResultList();
    }

    public List<ContactSearchResult> getGroupMembers(String groupId, String projectId, String userId) {
        validateGroupAndProject(projectId, userId, groupId);

        Group group = Group.findById(groupId);
        if (group == null || group.members == null) {
            return Collections.emptyList();
        }

        return group.members.stream()
                .map(contact -> {
                    if (contact instanceof NaturalContact naturalContact) {
                        return new NaturalContactSearchDTO(
                                naturalContact.id,
                                naturalContact.firstName,
                                naturalContact.lastName,
                                naturalContact.mailAddress
                        );
                    } else if (contact instanceof CompanyContact companyContact) {
                        return new CompanyContactSearchDTO(
                                companyContact.id,
                                companyContact.companyName,
                                companyContact.mailAddress
                        );
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Validates project existence and user permissions.
     */
    private void validateProjectAndAccount(String projectId, String accountId) {
        if (projectId == null || projectId.isBlank())
            throw new IllegalArgumentException("projectId must not be null or empty.");
        if (accountId == null || accountId.isBlank())
            throw new IllegalArgumentException("accountId must not be null or empty.");

        Long projectCount = em.createQuery(
                        "SELECT COUNT(p) FROM Project p WHERE p.id = :projectId", Long.class)
                .setParameter("projectId", projectId)
                .getSingleResult();

        if (projectCount == 0)
            throw new ProjectNotExistsException();

        if (!permissionService.hasPermission(projectId, accountId)) {
            throw new SecurityException("User does not have permission to access this project.");
        }
    }

    /**
     * Validates group existence within a project.
     */
    private void validateGroupExistence(String projectId, String groupId) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must not be null or empty.");

        Group group = Group.findById(groupId);
        if (group == null)
            throw new IllegalArgumentException("Group with ID " + groupId + " does not exist.");

        if (!group.project.id.equals(projectId)) {
            throw new IllegalArgumentException("Group does not belong to the specified project.");
        }
    }

    /**
     * Validates group ID.
     */
    private void validateGroupId(String groupId) {
        if (groupId == null || groupId.isBlank())
            throw new IllegalArgumentException("groupId must not be null or empty.");
    }

    /**
     * Validates group name.
     */
    private void validateGroupName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be null or empty.");
    }

    /**
     * Validates group existence and project association.
     */
    private void validateGroupAndProject(String projectId, String userId, String groupId) {
        validateProjectAndAccount(projectId, userId);
        validateGroupExistence(projectId, groupId);
    }

    /**
     * Fetches existing contacts based on member IDs.
     */
    private HashSet<Contact> fetchExistingContacts(List<?> members) {
        List<String> memberIds = members.stream()
                .map(member -> {
                    if (member instanceof NaturalContactSearchDTO naturalDTO) {
                        return naturalDTO.id();
                    } else if (member instanceof CompanyContactSearchDTO companyDTO) {
                        return companyDTO.id();
                    } else {
                        throw new IllegalArgumentException("Unknown member type.");
                    }
                })
                .collect(Collectors.toList());

        return em.createQuery(
                        "SELECT c FROM Contact c WHERE c.id IN :memberIds",
                        Contact.class)
                .setParameter("memberIds", memberIds)
                .getResultList().stream().collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Adds Keycloak users to the member list if they don't already exist.
     */
    private void addKeycloakUsers(HashSet<Contact> memberList, List<?> members) {
        for (Object memberDTO : members) {
            String memberId;
            if (memberDTO instanceof NaturalContactSearchDTO naturalDTO) {
                memberId = naturalDTO.id();
            } else if (memberDTO instanceof CompanyContactSearchDTO companyDTO) {
                memberId = companyDTO.id();
            } else {
                throw new IllegalArgumentException("Unknown member type.");
            }

            Contact existingContact = Contact.findById(memberId);
            if (existingContact == null) {
                // Add new contact fetched from Keycloak
                if (memberDTO instanceof NaturalContactSearchDTO) {
                    NaturalContactSearchDTO userDTO = keycloakAdminService.findUserAsNaturalContactSearchDTO(memberId);
                    if (userDTO != null) {
                        NaturalContact newContact = new NaturalContact();
                        newContact.firstName = userDTO.firstName();
                        newContact.lastName = userDTO.lastName();
                        newContact.mailAddress = userDTO.mailAddress();
                        newContact.kcUser = true;
                        newContact.id = userDTO.id();
                        em.persist(newContact);
                        // Add to memberList only if not already present
                        if (!memberList.contains(newContact)) {
                            memberList.add(newContact);
                        }
                    } else {
                        LOGGER.warnf("User with ID %s not found in Keycloak.", memberId);
                    }
                }
            } else {
                // Ensure not to add duplicate contacts
                if (!memberList.contains(existingContact)) {
                    memberList.add(existingContact);
                }
            }
        }
    }

    /**
     * Validates that all members are of known contact types.
     */
    private void validateMembersContactType(List<?> members) {
        if (members.isEmpty()) return;

        Object firstMember = members.get(0);
        String expectedType;
        if (firstMember instanceof NaturalContactSearchDTO) {
            expectedType = "natural";
        } else if (firstMember instanceof CompanyContactSearchDTO) {
            expectedType = "company";
        } else {
            throw new IllegalArgumentException("Unknown member type.");
        }

        for (Object member : members) {
            if ((expectedType.equals("natural") && !(member instanceof NaturalContactSearchDTO)) ||
                    (expectedType.equals("company") && !(member instanceof CompanyContactSearchDTO))) {
                throw new IllegalArgumentException("All members must be of type " + expectedType + ".");
            }
        }
    }

    /**
     * Validates that the group name is unique within the project.
     */
    private void validateUniqueGroupName(String projectId, String name) {
        Long count = em.createQuery(
                        "SELECT COUNT(g) FROM Group g WHERE g.project.id = :projectId AND g.name = :name",
                        Long.class)
                .setParameter("projectId", projectId)
                .setParameter("name", name)
                .getSingleResult();

        if (count > 0) {
            LOGGER.warnf("Conflict: Group name '%s' already exists in project '%s'.", name, projectId);
            throw new IllegalArgumentException("Group name already exists within the project.");
        }
    }

    /**
     * Validates that the group name is unique within the project during an update.
     */
    private void validateUniqueGroupNameForUpdate(String projectId, String groupId, String name) {
        Long count = em.createQuery(
                        "SELECT COUNT(g) FROM Group g WHERE g.project.id = :projectId AND g.name = :name AND g.id <> :groupId",
                        Long.class)
                .setParameter("projectId", projectId)
                .setParameter("name", name)
                .setParameter("groupId", groupId)
                .getSingleResult();

        if (count > 0) {
            LOGGER.warnf("Conflict: Group name '%s' already exists in project '%s'.", name, projectId);
            throw new IllegalArgumentException("Group name already exists within the project.");
        }
    }
}