package at.htlleonding.leomail.model.dto.template;

public record TemplateMetaInformationDTO(String templateName, String mailHeadline, String mailContent, Long greetingId, String greetingContent) {
}
