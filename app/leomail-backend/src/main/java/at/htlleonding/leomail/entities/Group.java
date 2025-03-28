package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

@Entity
@Table(name = "Groups", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "project_id"}))
public class Group extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public String id;

    @Column(nullable = false)
    public String name;

    @Column(nullable = true)
    public String description;

    @ManyToOne(fetch = FetchType.LAZY)
    public NaturalContact createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    public Project project;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    public Set<Contact> members = new HashSet<>();

    public Group() {
    }

    public Group(String name, String description, NaturalContact createdBy, Project project) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.project = project;
    }

    public Group(String name, String description, NaturalContact createdBy, Project project, HashSet<Contact> members) {
        this(name, description, createdBy, project);
        this.members = members;
    }

    public Set<Contact> getMembers() {
        return members;
    }
}
