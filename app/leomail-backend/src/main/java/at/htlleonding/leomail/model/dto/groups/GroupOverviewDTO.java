package at.htlleonding.leomail.model.dto.groups;

import at.htlleonding.leomail.model.enums.GroupType;

import java.io.Serializable;

public record GroupOverviewDTO(
        String id,
        String name,
        String creator
) implements Serializable {
}