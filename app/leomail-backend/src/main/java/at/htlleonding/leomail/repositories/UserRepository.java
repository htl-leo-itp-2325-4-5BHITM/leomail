package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Contact;
import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.model.dto.ProfileDTO;
import at.htlleonding.leomail.model.dto.user.AvailableMailAddressesDTO;
import at.htlleonding.leomail.model.dto.user.MailAddressInformationDTO;
import at.htlleonding.leomail.services.PermissionService;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class UserRepository implements PanacheRepository<NaturalContact> {

    @Inject
    PermissionService permissionService;

    public boolean checkUser(String sub) {
        Contact contact = Contact.findById(sub);
        if(contact == null) throw new RuntimeException("User not found");
        return contact.kcUser;
    }

    public ProfileDTO getProfile(String id) {
        NaturalContact contact = Contact.findById(id);
        if(contact == null) throw new RuntimeException("User not found");
        return new ProfileDTO(contact.firstName, contact.lastName, contact.mailAddress, "contact.schoolClass", "contact.departement");
    }

    /**
     * Findet einen Benutzer anhand der E-Mail-Adresse.
     *
     * @param email Die E-Mail-Adresse des Benutzers.
     * @return Der Benutzer oder null, wenn nicht gefunden.
     */
    public NaturalContact findByEmail(String email) {
        return find("mailAddress", email).firstResult();
    }

    /**
     * Aktualisiert das verschlüsselte Outlook-Passwort eines Benutzers.
     *
     * @param email             Die E-Mail-Adresse des Benutzers.
     * @param encryptedPassword Das verschlüsselte Passwort.
     */
    public void updateUserOutlookPassword(String email, String encryptedPassword) {
        NaturalContact user = findByEmail(email);
        if (user != null) {
            user.encryptedOutlookPassword = encryptedPassword;
            persist(user);
        }
    }

    public AvailableMailAddressesDTO getAvailableMailAddresses(String sub, String pid) {
        if (!permissionService.hasPermission(sub, pid)) {
            throw new RuntimeException("Permission denied");
        }

        NaturalContact contact = NaturalContact.findById(sub);
        Project project = Project.findById(pid);
        return new AvailableMailAddressesDTO(new MailAddressInformationDTO(contact.firstName + " " + contact.lastName, contact.mailAddress, contact.id),
                new MailAddressInformationDTO(project.name, project.mailAddress, project.id));
    }
}
