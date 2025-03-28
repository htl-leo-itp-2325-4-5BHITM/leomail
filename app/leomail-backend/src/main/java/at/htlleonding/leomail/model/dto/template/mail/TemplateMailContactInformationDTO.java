package at.htlleonding.leomail.model.dto.template.mail;

import at.htlleonding.leomail.model.enums.ContactType;

public record TemplateMailContactInformationDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress,
        ContactType contactType
) {
}