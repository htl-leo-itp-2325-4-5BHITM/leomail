package at.htlleonding.leomail.model.dto.groups;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.model.dto.contacts.CreatorDTO;
import at.htlleonding.leomail.model.enums.GroupType;
import java.io.Serializable;
import java.util.List;

public record GroupDetailDTO(
        String id,
        String name,
        String description,
        CreatorDTO createdBy,
        List<ContactSearchResult> members
) implements Serializable {

    public GroupDetailDTO(String id, String name, String description) {
        this(id, name, description, null, null);
    }

    public GroupDetailDTO(String id, String name, String description, CreatorDTO createdBy) {
        this(id, name, description, createdBy, null);
    }
}