package at.htlleonding.leomail.model.dto.contacts;

import at.htlleonding.leomail.contracts.ContactSearchResult;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("company") // Must match the name in @JsonSubTypes.Type
public record CompanyContactSearchDTO(
        String id,
        String companyName,
        String mailAddress
) implements ContactSearchResult {
    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return companyName;
    }

    @Override
    public String getMailAddress() {
        return mailAddress;
    }
}