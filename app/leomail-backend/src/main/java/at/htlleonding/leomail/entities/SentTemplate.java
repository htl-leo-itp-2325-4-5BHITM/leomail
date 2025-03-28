package at.htlleonding.leomail.entities;

import at.htlleonding.leomail.model.MailType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
public class SentTemplate extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    public Template template;

    public LocalDateTime scheduledAt;

    public LocalDateTime sentOn;

    @ManyToOne(fetch = FetchType.LAZY)
    public Project project;

    @OneToMany(mappedBy = "usedTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<SentMail> mails = new ArrayList<>();

    @OneToMany(mappedBy = "sentTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<Attachment> attachments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MailType mailType;

    @Column(nullable = false)
    public String senderId;

    public SentTemplate() {}

    public SentTemplate(Template template, LocalDateTime scheduledAt, Project project, MailType mailType, String senderId) {
        this.template = template;
        this.scheduledAt = scheduledAt;
        this.project = project;
        this.mailType = mailType;
        this.senderId = senderId;
    }
}
