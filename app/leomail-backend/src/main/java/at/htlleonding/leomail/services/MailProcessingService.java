package at.htlleonding.leomail.services;

import at.htlleonding.leomail.entities.Attachment;
import at.htlleonding.leomail.model.SMTPInformation;
import at.htlleonding.leomail.repositories.MailRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MailProcessingService {

    @Inject
    MailRepository repository;

    @Inject
    JsonWebToken jwt;

    @Inject
    StorageService storageService;

    @Inject
    PermissionService permissionService;

    private static final org.jboss.logging.Logger LOGGER = org.jboss.logging.Logger.getLogger(MailProcessingService.class);

    public void sendMailByTemplateWithAttachments(MultipartFormDataInput input) throws Exception {
        Map<String, List<InputPart>> uploadForm = input.getFormDataMap();

        // Extrahiere projectId
        String projectId = getValue(uploadForm, "projectId");
        if (projectId == null || projectId.trim().isEmpty()) {
            throw new IllegalArgumentException("projectId fehlt");
        }

        // SMTP-Informationen parsen
        String smtpInfoJson = getValue(uploadForm, "smtpInformation");
        SMTPInformation smtpInformation;
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            smtpInformation = mapper.readValue(smtpInfoJson, SMTPInformation.class);
        } catch (Exception e) {
            LOGGER.error("Ungültiges SMTP-Information JSON: ", e);
            throw new IllegalArgumentException("Ungültiges SMTP-Information JSON: " + e.getMessage());
        }

        // Anhänge und Metadaten extrahieren
        List<InputPart> attachmentParts = uploadForm.get("attachments");
        List<String> fileNames = getValues(uploadForm, "fileName");
        List<String> contentTypes = getValues(uploadForm, "contentType");
        List<String> sizesStr = getValues(uploadForm, "size");

        if (attachmentParts == null) {
            attachmentParts = new ArrayList<>();
        }
        int numAttachments = attachmentParts.size();
        List<String> safeFileNames = new ArrayList<>();
        List<String> safeContentTypes = new ArrayList<>();
        List<String> safeSizesStr = new ArrayList<>();

        for (int i = 0; i < numAttachments; i++) {
            safeFileNames.add((fileNames != null && fileNames.size() > i)
                    ? fileNames.get(i)
                    : extractFileName(attachmentParts.get(i)));
            safeContentTypes.add((contentTypes != null && contentTypes.size() > i)
                    ? contentTypes.get(i)
                    : "application/octet-stream");
            safeSizesStr.add((sizesStr != null && sizesStr.size() > i)
                    ? sizesStr.get(i)
                    : "-1");
        }

        List<Attachment> attachments = new ArrayList<>();
        String userId = jwt.getClaim("sub");

        for (int i = 0; i < numAttachments; i++) {
            InputPart part = attachmentParts.get(i);
            String fileName = safeFileNames.get(i);
            String contentType = safeContentTypes.get(i);
            long size;
            try {
                size = Long.parseLong(safeSizesStr.get(i));
            } catch (NumberFormatException e) {
                LOGGER.error("Ungültige Größe für Anhang: " + safeSizesStr.get(i), e);
                throw new IllegalArgumentException("Ungültige Größe für Anhang: " + safeSizesStr.get(i));
            }
            try (InputStream fileStream = part.getBody(InputStream.class, null)) {
                LOGGER.infof("Uploading file: %s, Content-Type: %s, Size: %d bytes", fileName, contentType, size);
                String objectName = storageService.uploadFile(fileStream, fileName, contentType, size);
                Attachment attachment = new Attachment(null, fileName, objectName, contentType, size, userId);
                attachments.add(attachment);
            } catch (Exception e) {
                LOGGER.error("Fehler beim Hochladen der Anhänge: ", e);
                throw new Exception("Fehler beim Hochladen der Anhänge: " + e.getMessage());
            }
        }

        // Versand der E-Mails über das Repository
        repository.sendMailsByTemplate(projectId, userId, smtpInformation, attachments);
    }

    public Response getAttachment(Long id) {
        Attachment attachment = Attachment.findById(id);
        if (attachment == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Anhang nicht gefunden")
                    .build();
        }
        String userId = jwt.getClaim("sub");
        if (!attachment.ownerId.equals(userId) && !permissionService.hasPermissionForAttachment(userId, attachment)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Zugriff verweigert")
                    .build();
        }
        try {
            InputStream fileStream = storageService.downloadFile(attachment.filePath);
            byte[] buffer = fileStream.readNBytes(10);
            if (buffer.length == 0) {
                LOGGER.warn("Download-Anhang '" + attachment.fileName + "' ist leer.");
            } else {
                LOGGER.infof("Download-Anhang '%s' enthält mindestens %d Bytes.", attachment.fileName, buffer.length);
            }
            InputStream downloadStream = new SequenceInputStream(new ByteArrayInputStream(buffer), fileStream);
            return Response.ok(downloadStream)
                    .header("Content-Disposition", "attachment; filename=\"" + attachment.fileName + "\"")
                    .header("Content-Length", attachment.size)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Fehler beim Abrufen der Datei: ", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Fehler beim Abrufen der Datei: " + e.getMessage())
                    .build();
        }
    }

    // --- Hilfsmethoden zum Extrahieren von Parametern aus dem Multipart-Formular ---

    private String extractFileName(InputPart part) {
        try {
            String header = part.getHeaders().getFirst("Content-Disposition");
            if (header != null) {
                for (String content : header.split(";")) {
                    if (content.trim().startsWith("filename")) {
                        String[] nameParts = content.split("=");
                        if (nameParts.length > 1) {
                            return nameParts[1].trim().replaceAll("\"", "");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Konnte Dateinamen nicht extrahieren.", e);
        }
        return "unknown";
    }

    private String getValue(Map<String, List<InputPart>> uploadForm, String key) throws Exception {
        List<InputPart> parts = uploadForm.get(key);
        if (parts != null && !parts.isEmpty()) {
            return parts.get(0).getBodyAsString();
        }
        return null;
    }

    private List<String> getValues(Map<String, List<InputPart>> uploadForm, String key) throws Exception {
        List<InputPart> parts = uploadForm.get(key);
        if (parts != null && !parts.isEmpty()) {
            List<String> values = new ArrayList<>();
            for (InputPart part : parts) {
                values.add(part.getBodyAsString());
            }
            return values;
        }
        return null;
    }
}
