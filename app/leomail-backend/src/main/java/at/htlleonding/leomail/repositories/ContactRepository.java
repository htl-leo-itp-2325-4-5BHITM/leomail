package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.entities.CompanyContact;
import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.entities.Template; // Ensure you have this entity
import at.htlleonding.leomail.model.dto.contacts.*;
import at.htlleonding.leomail.model.exceptions.ObjectContainsNullAttributesException;
import at.htlleonding.leomail.model.exceptions.contacts.ContactExistsInKeycloakException;
import at.htlleonding.leomail.model.exceptions.contacts.ContactKcUserException;
import at.htlleonding.leomail.services.KeycloakAdminService;
import at.htlleonding.leomail.services.Utilities;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContactRepository {

    private static final Logger LOGGER = Logger.getLogger(ContactRepository.class);

    @Inject
    KeycloakAdminService keycloakAdminService;

    public boolean userExists(String id) {
        return NaturalContact.count("id", id) > 0;
    }

    /**
     * Adds a new natural contact.
     *
     * @param contactDTO Data of the contact to be added
     */
    @Transactional
    public void addContact(NaturalContactAddDTO contactDTO) {
        if (NaturalContact.count("mailAddress", contactDTO.mailAddress()) > 0) {
            throw new ContactExistsInKeycloakException("Mail address already exists in database");
        }

        Utilities.setEmptyStringsToNull(contactDTO);

        List<String> nullFields = Utilities.listNullFields(contactDTO, List.of("id", "prefixTitle", "suffixTitle", "company", "positionAtCompany"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        NaturalContact naturalContact = new NaturalContact();
        naturalContact.firstName = contactDTO.firstName().trim();
        naturalContact.lastName = contactDTO.lastName().trim();
        naturalContact.mailAddress = contactDTO.mailAddress().trim();
        naturalContact.prefixTitle = contactDTO.prefixTitle() != null ? contactDTO.prefixTitle().trim() : null;
        naturalContact.suffixTitle = contactDTO.suffixTitle() != null ? contactDTO.suffixTitle().trim() : null;
        naturalContact.company = contactDTO.company() != null ? contactDTO.company().trim() : null;
        naturalContact.positionAtCompany = contactDTO.positionAtCompany() != null ? contactDTO.positionAtCompany().trim() : null;
        naturalContact.gender = contactDTO.gender();

        naturalContact.persist();
        LOGGER.infof("New natural contact '%s' successfully added.", naturalContact);
    }

    /**
     * Adds a new company contact.
     *
     * @param contactDTO Data of the contact to be added
     */
    public void addContact(CompanyContactAddDTO contactDTO) {
        if (CompanyContact.count("mailAddress", contactDTO.mailAddress()) > 0) {
            throw new ContactExistsInKeycloakException("Mail address already exists in database");
        }

        Utilities.setEmptyStringsToNull(contactDTO);

        List<String> nullFields = Utilities.listNullFields(contactDTO, List.of("id"));
        if (!nullFields.isEmpty()) {
            throw new ObjectContainsNullAttributesException(nullFields);
        }

        CompanyContact companyContact = new CompanyContact();
        companyContact.companyName = contactDTO.companyName().trim();
        companyContact.mailAddress = contactDTO.mailAddress().trim();

        companyContact.persist();
        LOGGER.infof("New company contact '%s' successfully added.", companyContact);
    }

    /**
     * Updates an existing natural contact.
     *
     * @param contactDTO Data of the contact to be updated
     */
    @Transactional
    public void updateContact(NaturalContactDetailDTO contactDTO) {
        NaturalContact contact = NaturalContact.findById(contactDTO.id());
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        if (contact.kcUser) {
            throw new IllegalArgumentException("Cannot update Keycloak user");
        }

        contact.mailAddress = contactDTO.mailAddress().trim();
        contact.firstName = contactDTO.firstName().trim();
        contact.lastName = contactDTO.lastName().trim();
        contact.gender = contactDTO.gender();
        contact.suffixTitle = contactDTO.suffixTitle() != null ? contactDTO.suffixTitle().trim() : null;
        contact.prefixTitle = contactDTO.prefixTitle() != null ? contactDTO.prefixTitle().trim() : null;
        contact.company = contactDTO.company() != null ? contactDTO.company().trim() : null;
        contact.positionAtCompany = contactDTO.positionAtCompany() != null ? contactDTO.positionAtCompany().trim() : null;

        LOGGER.infof("Natural contact with ID %s successfully updated.", contactDTO.id());
    }

    /**
     * Updates an existing company contact.
     *
     * @param contactDTO Data of the contact to be updated
     */
    @Transactional
    public void updateContact(CompanyContactDetailDTO contactDTO) {
        CompanyContact contact = CompanyContact.findById(contactDTO.id());
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }
        if (contact.kcUser) {
            throw new IllegalArgumentException("Cannot update Keycloak user");
        }

        contact.mailAddress = contactDTO.mailAddress().trim();
        contact.companyName = contactDTO.companyName().trim();

        LOGGER.infof("Company contact with ID %s successfully updated.", contactDTO.id());
    }

    /**
     * Deletes a contact by ID.
     *
     * @param id ID of the contact to be deleted
     */
    @Transactional
    public void deleteContact(String id) {
        Contact contact = Contact.findById(id);

        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        }

        if (contact.kcUser) {
            throw new ContactKcUserException("Cannot delete a Keycloak user contact");
        }

        contact.delete();
        LOGGER.infof("Contact with ID %s successfully deleted.", id);
    }

    /**
     * Counts the number of templates referencing a given contact.
     *
     * @param contactId ID of the contact
     * @return Number of referencing templates
     */
    public long countTemplatesReferencingContact(String contactId) {
        // Assuming you have a Template entity with a 'contact' field
        return Template.count("contact.id", contactId);
    }

    /**
     * Retrieves contact details by ID.
     *
     * @param id ID of the contact
     * @return Contact details
     */
    public Object getContact(String id) {
        Contact contact = Contact.findById(id);
        if (contact == null) {
            throw new IllegalArgumentException("Contact not found");
        } else if (contact instanceof NaturalContact) {
            NaturalContact naturalContact = (NaturalContact) contact;
            return new NaturalContactDetailDTO(
                    naturalContact.id,
                    naturalContact.firstName,
                    naturalContact.lastName,
                    naturalContact.mailAddress,
                    naturalContact.prefixTitle,
                    naturalContact.suffixTitle,
                    naturalContact.company,
                    naturalContact.positionAtCompany,
                    naturalContact.gender,
                    naturalContact.kcUser
            );
        } else if (contact instanceof CompanyContact) {
            CompanyContact companyContact = (CompanyContact) contact;
            return new CompanyContactDetailDTO(
                    companyContact.id,
                    companyContact.companyName,
                    companyContact.mailAddress,
                    companyContact.kcUser
            );
        } else {
            throw new IllegalArgumentException("Unknown contact type");
        }
    }

    /**
     * Searches natural contacts based on a search term.
     *
     * @param searchTerm Search term
     * @param own        Own user ID to exclude
     * @return List of found natural contacts
     */
    public List<NaturalContactSearchDTO> searchNaturalContacts(String searchTerm, String own) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            searchTerm = "";
        }
        String finalSearchTerm = "%" + searchTerm.trim().toLowerCase() + "%";

        List<NaturalContact> naturalContacts = NaturalContact.find(
                "(lower(mailAddress) like ?1 or lower(firstName) like ?1 or lower(lastName) like ?1) and id != ?2",
                finalSearchTerm,
                own
        ).list();

        return naturalContacts.stream()
                .map(contact -> new NaturalContactSearchDTO(
                        contact.id,
                        contact.firstName,
                        contact.lastName,
                        contact.mailAddress
                ))
                .collect(Collectors.toList());
    }

    /**
     * Searches company contacts based on a search term.
     *
     * @param searchTerm Search term
     * @param own        Own user ID to exclude
     * @return List of found company contacts
     */
    public List<CompanyContactSearchDTO> searchCompanyContacts(String searchTerm, String own) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            searchTerm = "";
        }
        String finalSearchTerm = "%" + searchTerm.trim().toLowerCase() + "%";

        List<CompanyContact> companyContacts = CompanyContact.find(
                "(lower(mailAddress) like ?1 or lower(companyName) like ?1) and id != ?2",
                finalSearchTerm,
                own
        ).list();

        return companyContacts.stream()
                .map(contact -> new CompanyContactSearchDTO(
                        contact.id,
                        contact.companyName,
                        contact.mailAddress
                ))
                .collect(Collectors.toList());
    }

    /**
     * Searches all contacts (natural and company) based on a search term.
     *
     * @param searchTerm Search term
     * @param own        Own user ID to exclude
     * @return List of found contacts
     */
    public List<ContactSearchResult> searchAllContacts(String searchTerm, String own) {
        List<ContactSearchResult> results = new ArrayList<>();
        results.addAll(searchNaturalContacts(searchTerm, own));
        results.addAll(searchCompanyContacts(searchTerm, own));
        return results;
    }

    /**
     * Saves a Keycloak user locally as a natural contact.
     *
     * @param id        Unique ID
     * @param firstName First name
     * @param lastName  Last name
     * @param email     Email address
     * @return The saved NaturalContact
     */
    @Transactional
    public NaturalContact saveKeycloakUserLocally(String id, String firstName, String lastName, String email) {
        if (NaturalContact.count("mailAddress", email) > 0) {
            throw new ContactExistsInKeycloakException("Mail address already exists in database");
        }

        NaturalContact contact = new NaturalContact();
        contact.id = id;
        contact.firstName = firstName.trim();
        contact.lastName = lastName.trim();
        contact.mailAddress = email.trim();
        contact.kcUser = true;
        contact.persist();
        LOGGER.infof("New Keycloak user '%s' successfully saved locally.", contact);
        return contact;
    }
}