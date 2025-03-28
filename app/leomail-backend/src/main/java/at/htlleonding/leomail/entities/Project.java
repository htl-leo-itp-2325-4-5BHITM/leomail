package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Project extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    public String name;
    public String description;
    public LocalDateTime createdOn;

    public String mailAddress;
    public String password;

    @ManyToOne(fetch = FetchType.LAZY)
    public NaturalContact createdBy;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Group> groups = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<Template> templates = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    public List<SentTemplate> sentTemplates = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_contact",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    public List<NaturalContact> members = new ArrayList<>();

    public Project() {
        this.createdOn = LocalDateTime.now();
    }

    public Project(String name, String description, NaturalContact createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdOn = LocalDateTime.now();
    }

    public Project(String name, String description, NaturalContact createdBy, String mailAddress, String password, List<NaturalContact> members) {
        this(name, description, createdBy);
        this.mailAddress = mailAddress;
        this.password = password;
        this.members = members;
    }
}
