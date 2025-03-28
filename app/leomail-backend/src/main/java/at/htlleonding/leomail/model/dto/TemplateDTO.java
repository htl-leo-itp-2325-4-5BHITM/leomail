package at.htlleonding.leomail.model.dto;

import java.io.Serializable;

public record TemplateDTO(String id, String name, String headline, String content, boolean filesRequired, Long greeting, String accountName, String projectId) implements Serializable {

}
