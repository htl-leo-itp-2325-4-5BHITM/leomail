package at.htlleonding.leomail.listeners;

import at.htlleonding.leomail.entities.Attachment;
import at.htlleonding.leomail.services.StorageService;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.persistence.PreRemove;
import org.jboss.logging.Logger;

public class AttachmentEntityListener {

    private static final Logger LOGGER = Logger.getLogger(AttachmentEntityListener.class);

    @PreRemove
    public void preRemove(Attachment attachment) {
        StorageService storageService = CDI.current().select(StorageService.class).get();
        try {
            storageService.deleteFile(attachment.filePath);
            LOGGER.infof("Datei '%s' aus MinIO gelöscht.", attachment.filePath);
        } catch (Exception e) {
            LOGGER.errorf("Fehler beim Löschen der Datei '%s' aus MinIO.", attachment.filePath, e);
            throw new RuntimeException("Fehler beim Löschen der Datei aus MinIO", e);
        }
    }
}
