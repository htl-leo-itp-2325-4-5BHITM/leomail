package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
public class TemplateGreeting extends PanacheEntity {

    @Column(nullable = false, length = 1024)
    public String templateString;

    @Column(length = 2048)
    public String content;

    public TemplateGreeting() {
    }

    public TemplateGreeting(Long id, String content) {
        this.id = id;
        this.content = content;
    }
}