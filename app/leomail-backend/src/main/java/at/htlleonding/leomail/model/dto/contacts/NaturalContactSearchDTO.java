package at.htlleonding.leomail.model.dto.contacts;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import at.htlleonding.leomail.entities.Contact;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("natural") // Must match the name in @JsonSubTypes.Type
public record NaturalContactSearchDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress
) implements ContactSearchResult {

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getMailAddress() {
        return mailAddress;
    }
}
