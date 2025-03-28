package at.htlleonding.leomail.model.dto;

import at.htlleonding.leomail.model.dto.template.SentMailDTO;
import at.htlleonding.leomail.model.dto.template.TemplateAccountInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateDateInformationDTO;
import at.htlleonding.leomail.model.dto.template.TemplateMetaInformationDTO;

import java.io.Serializable;
import java.util.List;

public record UsedTemplateDTO(
        String id,
        TemplateMetaInformationDTO meta,
        TemplateDateInformationDTO keyDates,
        TemplateAccountInformationDTO accountInformation,
        List<SentMailDTO> mails,
        List<AttachmentResponseDTO> attachments
) implements Serializable {}