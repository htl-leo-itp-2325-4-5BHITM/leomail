package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Attachment;
import at.htlleonding.leomail.entities.Project;
import at.htlleonding.leomail.entities.Template;
import at.htlleonding.leomail.repositories.ProjectRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PermissionService {

    private static final Logger LOGGER = Logger.getLogger(PermissionService.class);

    @Inject
    ProjectRepository projectRepository;

    /**
     * Überprüft, ob ein Nutzer Berechtigung für ein Projekt hat.
     *
     * @param projectId ID des Projekts.
     * @param userId    ID des Nutzers.
     * @return true, wenn der Nutzer Berechtigung hat, sonst false.
     */
    public boolean hasPermission(String projectId, String userId) {
        // Implementieren Sie die Logik zur Überprüfung der Berechtigungen.
        // Dies könnte die Überprüfung von Rollen, Mitgliedschaften usw. umfassen.
        // Beispiel:
        Project project = Project.findById(projectId);
        if (project == null) {
            return false;
        }
        return project.members.stream().anyMatch(member -> member.id.equals(userId)) || project.createdBy.id.equals(userId);
    }

    /**
     * Überprüft, ob ein Nutzer Berechtigung für einen Anhang hat.
     *
     * @param userId    ID des Nutzers.
     * @param attachment Der Anhang.
     * @return true, wenn der Nutzer Berechtigung hat, sonst false.
     */
    public boolean hasPermissionForAttachment(String userId, Attachment attachment) {
        return attachment.ownerId.equals(userId) || hasPermission(getProjectIdFromAttachment(attachment), userId);
    }

    private String getProjectIdFromAttachment(Attachment attachment) {
        if (attachment.sentTemplate != null && attachment.sentTemplate.project != null) {
            return attachment.sentTemplate.project.id;
        }
        return null;
    }
}