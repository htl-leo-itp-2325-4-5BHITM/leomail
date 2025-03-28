package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.NaturalContact;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.logging.Logger;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class KeycloakAdminService {

    private static final Logger LOGGER = Logger.getLogger(KeycloakAdminService.class);

    @Inject
    Keycloak keycloakClient;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.profile")
    String profile;

    @Inject
    ImportStatusService importStatusService;

    @Inject
    ManagedExecutor managedExecutor;

    /**
     * Beobachtet das StartupEvent und startet den Importprozess asynchron.
     */
    public void onStart(@Observes StartupEvent event) {
        LOGGER.info("StartupEvent empfangen. Initialisiere asynchronen Import.");
        importUsersAsync();
    }

    /**
     * Führt den Import der Nutzer asynchron aus.
     */
    @ActivateRequestContext
    public void importUsersAsync() {
        managedExecutor.runAsync(this::saveAllUsersToAppDb).thenRun(() -> {
            LOGGER.info("Asynchroner Import abgeschlossen.");
        }).exceptionally(ex -> {
            LOGGER.error("Fehler beim asynchronen Import", ex);
            return null;
        });
    }

    /**
     * Finds a user by user ID and returns a NaturalContactSearchDTO.
     *
     * @param userId User ID
     * @return NaturalContactSearchDTO object or null if not found
     */
    public NaturalContactSearchDTO findUserAsNaturalContactSearchDTO(String userId) {
        try {
            UserRepresentation user = keycloakClient.realm(realm)
                    .users().get(userId).toRepresentation();
            if (user == null) {
                return null;
            }
            return new NaturalContactSearchDTO(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            );
        } catch (Exception e) {
            LOGGER.error("Error finding user", e);
            throw new RuntimeException("Error finding user", e);
        }
    }

    /**
     * Importiert alle Nutzer in die Anwendungsdatenbank.
     */
    public void saveAllUsersToAppDb() {
        LOGGER.info("Starte saveAllUsersToAppDb Methode");
        if (!isProduction()) {
            LOGGER.info("Nicht im Produktionsmodus. Import der Keycloak-Nutzer wird übersprungen.");
            return;
        }

        LOGGER.info("Im Produktionsmodus. Starte Import der Keycloak-Nutzer.");
        importStatusService.setImporting(true);
        try {
            int first = 0;
            int max = 2000;
            List<UserRepresentation> usersBatch;
            do {
                usersBatch = keycloakClient.realm(realm).users().list(first, max);
                LOGGER.info("Importiere Batch von " + first + " bis " + (first + max));
                for (UserRepresentation user : usersBatch) {
                    saveOrUpdateKeycloakUser(user);
                }
                first += max;
            } while (!usersBatch.isEmpty());
            LOGGER.info("Alle Keycloak-Nutzer wurden in die Anwendungsdatenbank importiert.");
        } catch (Exception e) {
            LOGGER.error("Fehler beim Importieren der Keycloak-Nutzer", e);
        } finally {
            importStatusService.setImporting(false);
            LOGGER.info("Importstatus auf false gesetzt.");
        }
    }

    private boolean isProduction() {
        return "prod".equals(profile);
    }


    /**
     * Saves or updates a Keycloak user in the application database.
     *
     * @param user UserRepresentation to be saved or updated
     */
    @Transactional
    public void saveOrUpdateKeycloakUser(UserRepresentation user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            LOGGER.warnf("User '%s' hat keine gültige E-Mail-Adresse. Überspringe das Speichern.", user.getId());
            return;
        }

        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            LOGGER.warnf("User '%s' hat keine gültige E-Mail-Adresse. Überspringe das Speichern.", user.getId());
            return;
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            LOGGER.warnf("User '%s' hat keine gültige E-Mail-Adresse. Überspringe das Speichern.", user.getId());
            return;
        }

        NaturalContact existingContact = NaturalContact.findById(user.getId());
        if (existingContact != null) {
            existingContact.firstName = user.getFirstName() != null ? user.getFirstName() : "";
            existingContact.lastName = user.getLastName() != null ? user.getLastName() : "";
            existingContact.mailAddress = user.getEmail();
            existingContact.persist();
            LOGGER.infof("Aktualisierter bestehender Keycloak-Benutzer '%s' in der Anwendungsdatenbank.", existingContact.id);
        } else {
            NaturalContact contact = new NaturalContact();
            contact.id = user.getId();
            contact.firstName = user.getFirstName() != null ? user.getFirstName() : "";
            contact.lastName = user.getLastName() != null ? user.getLastName() : "";
            contact.mailAddress = user.getEmail();
            contact.kcUser = true;

            contact.persist();
            LOGGER.infof("Neuer Keycloak-Benutzer '%s' erfolgreich in der Anwendungsdatenbank gespeichert.", contact.id);
        }
    }
}