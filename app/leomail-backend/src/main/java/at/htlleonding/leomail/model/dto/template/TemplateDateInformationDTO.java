package at.htlleonding.leomail.model.dto.template;

import java.io.Serializable;
import java.time.LocalDateTime;

public record TemplateDateInformationDTO(LocalDateTime created, LocalDateTime sentOn, LocalDateTime scheduledAt) implements Serializable {
}
