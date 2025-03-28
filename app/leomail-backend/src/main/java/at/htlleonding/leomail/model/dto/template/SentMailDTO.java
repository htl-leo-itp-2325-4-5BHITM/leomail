package at.htlleonding.leomail.model.dto.template;

import at.htlleonding.leomail.model.dto.template.mail.TemplateMailContactInformationDTO;

public record SentMailDTO(TemplateMailContactInformationDTO contact, String content) {

}
