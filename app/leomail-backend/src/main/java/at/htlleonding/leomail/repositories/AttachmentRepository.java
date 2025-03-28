package at.htlleonding.leomail.repositories;

import at.htlleonding.leomail.entities.Attachment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttachmentRepository implements PanacheRepository<Attachment> {
}
