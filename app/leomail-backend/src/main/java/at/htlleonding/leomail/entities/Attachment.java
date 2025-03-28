package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.listeners.AttachmentEntityListener;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@EntityListeners(AttachmentEntityListener.class)
public class Attachment extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    public SentTemplate sentTemplate;

    @Column(nullable = false)
    public String fileName;

    @Column(nullable = false)
    public String filePath;

    @Column(nullable = false)
    public String contentType;

    @Column(nullable = false)
    public Long size;

    @Column(nullable = false)
    public String ownerId;

    public Attachment() {}

    public Attachment(SentTemplate sentTemplate, String fileName, String filePath, String contentType, Long size, String userId) {
        this.sentTemplate = sentTemplate;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.size = size;
        this.ownerId = userId;
    }
}