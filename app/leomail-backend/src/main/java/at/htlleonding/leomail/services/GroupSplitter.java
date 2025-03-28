package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.Group;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import at.htlleonding.leomail.repositories.ContactRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class GroupSplitter {

    private static final Logger LOGGER = Logger.getLogger(GroupSplitter.class);

    @Inject
    KeycloakAdminService keycloakAdminService;

    @Inject
    ContactRepository contactRepository;

    /**
     * Retrieves all contacts based on the specified groups and contact IDs.
     *
     * @param groups   List of group IDs
     * @param contacts List of contact IDs
     * @return List of all relevant contacts
     */
    public List<Contact> getAllContacts(List<String> groups, List<String> contacts) {
        Set<Contact> allContacts = new HashSet<>();

        List<Contact> contactList = contacts.stream()
                .map(this::findOrCreateContact)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        allContacts.addAll(contactList);

        // Process groups
        for (String groupId : groups) {
            Group groupEntity = Group.findById(groupId);
            if (groupEntity != null && groupEntity.members != null) {
                allContacts.addAll(groupEntity.members);
            } else {
                LOGGER.warnf("Group with ID %s not found or has no members.", groupId);
            }
        }

        return new ArrayList<>(allContacts);
    }

    /**
     * Finds an existing contact by ID or creates a new one from Keycloak if not found.
     *
     * @param contactId ID of the contact
     * @return Contact instance or null if not found
     */
    private Contact findOrCreateContact(String contactId) {
        Contact contact = Contact.findById(contactId);
        if (contact == null) {
            NaturalContactSearchDTO userDTO = keycloakAdminService.findUserAsNaturalContactSearchDTO(contactId);
            if (userDTO != null) {
                contact = contactRepository.saveKeycloakUserLocally(userDTO.id(), userDTO.firstName(), userDTO.lastName(), userDTO.mailAddress());
            } else {
                LOGGER.warnf("User with ID %s not found in Keycloak.", contactId);
            }
        }
        return contact;
    }
}