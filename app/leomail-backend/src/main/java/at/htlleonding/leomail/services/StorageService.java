package at.htlleonding.leomail.services;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@ApplicationScoped
public class StorageService {

    private static final Logger LOGGER = Logger.getLogger(StorageService.class);

    @Inject
    @ConfigProperty(name = "minio.url")
    String minioUrl;

    @Inject
    @ConfigProperty(name = "minio.access-key")
    String accessKey;

    @Inject
    @ConfigProperty(name = "minio.secret-key")
    String secretKey;

    @Inject
    @ConfigProperty(name = "minio.bucket.name")
    String bucketName;

    MinioClient minioClient;

    @PostConstruct
    void init() {
        minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                LOGGER.infof("Bucket '%s' erstellt.", bucketName);
            } else {
                LOGGER.infof("Bucket '%s' existiert bereits.", bucketName);
            }
        } catch (Exception e) {
            LOGGER.error("Fehler bei der Initialisierung von MinIO", e);
            throw new RuntimeException("Fehler bei der Initialisierung von MinIO", e);
        }
    }

    /**
     * Lädt eine Datei zu MinIO hoch.
     *
     * @param stream      InputStream der Datei
     * @param fileName    Originaldateiname
     * @param contentType MIME-Typ der Datei
     * @return Der Name des gespeicherten Objekts in MinIO
     * @throws Exception bei Fehlern
     */
    public String uploadFile(InputStream stream, String fileName, String contentType, long size) throws Exception {
        if (size < 0) {
            byte[] data = stream.readAllBytes();
            size = data.length;
            stream = new ByteArrayInputStream(data);
        }
        String objectName = UUID.randomUUID().toString() + "_" + fileName;
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(stream, size, -1)
                        .contentType(contentType)
                        .build()
        );
        LOGGER.infof("Datei '%s' als Objekt '%s' hochgeladen. Größe: %d Bytes.", fileName, objectName, size);
        return objectName;
    }

    /**
     * Lädt eine Datei von MinIO herunter.
     *
     * @param objectName Name des Objekts in MinIO
     * @return InputStream der heruntergeladenen Datei
     * @throws Exception bei Fehlern
     */
    public InputStream downloadFile(String objectName) throws Exception {
        InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );

        StatObjectResponse stat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
        LOGGER.infof("Datei '%s' wird heruntergeladen. Größe: %d Bytes.", objectName, stat.size());

        return stream;
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            LOGGER.infof("Datei '%s' aus Bucket '%s' gelöscht.", objectName, bucketName);
        } catch (Exception e) {
            LOGGER.errorf("Fehler beim Löschen der Datei '%s' aus Bucket '%s'.", objectName, bucketName, e);
            throw new RuntimeException(e);
        }
    }
}