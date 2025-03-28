package at.htlleonding.leomail.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Contact extends PanacheEntityBase {

    @Id
    @Column(length = 36)
    public String id;

    @Column(nullable = false)
    public boolean kcUser = false;

    @CreationTimestamp
    public LocalDateTime created;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JsonIgnore
    public List<Group> groups = new ArrayList<>();

    public Contact() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public abstract String getMailAddress();
}