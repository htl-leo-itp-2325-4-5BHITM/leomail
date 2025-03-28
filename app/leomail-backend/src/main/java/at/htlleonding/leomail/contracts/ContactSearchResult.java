package at.htlleonding.leomail.contracts;

import at.htlleonding.leomail.model.dto.contacts.CompanyContactSearchDTO;
import at.htlleonding.leomail.model.dto.contacts.NaturalContactSearchDTO;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = NaturalContactSearchDTO.class, name = "natural"),
        @JsonSubTypes.Type(value = CompanyContactSearchDTO.class, name = "company")
})
public interface ContactSearchResult {
    String getId();
    String getDisplayName();
    String getMailAddress();
}
