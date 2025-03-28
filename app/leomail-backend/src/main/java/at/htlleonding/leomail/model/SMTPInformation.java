package at.htlleonding.leomail.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record SMTPInformation(
        Receiver receiver,
        String templateId,
        boolean personalized,
        LocalDateTime scheduledAt,
        FromMailDTO from
) implements Serializable {
}

