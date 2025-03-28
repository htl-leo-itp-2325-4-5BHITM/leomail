package at.htlleonding.leomail.model;

import java.io.Serializable;

public record FromMailDTO(MailType mailType, String id)implements Serializable {
}
