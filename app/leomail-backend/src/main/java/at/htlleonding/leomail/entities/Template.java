package at.htlleonding.leomail.entities;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
public class Template extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(length = 128, nullable = false, unique = true)
    public String name;

    @ManyToOne(fetch = FetchType.LAZY)
    public Project project;

    @CreationTimestamp
    public LocalDateTime created;

    @Column(length = 256, nullable = false)
    public String headline;

    @Column(length = 8192, nullable = false)
    public String content;

    @Column(nullable = false)
    public boolean filesRequired = false;

    @ManyToOne(fetch = FetchType.LAZY)
    public Contact createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    public TemplateGreeting greeting;

    public Template() {
        this.created = LocalDateTime.now();
    }

    public Template(String name, String headline, String content, boolean filesRequired, Contact createdBy, TemplateGreeting greeting) {
        this();
        this.name = name;
        this.headline = headline;
        this.content = content;
        this.filesRequired = filesRequired;
        this.createdBy = createdBy;
        this.greeting = greeting;
    }

    public Template(String name, String headline, String content, boolean filesRequired, Contact createdBy, TemplateGreeting greeting, String projectId) {
        this(name, headline, content, filesRequired, createdBy, greeting);
        this.project = Project.findById(projectId);
    }
}