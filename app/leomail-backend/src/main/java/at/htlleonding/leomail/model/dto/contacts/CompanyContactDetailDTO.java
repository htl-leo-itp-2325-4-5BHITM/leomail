package at.htlleonding.leomail.model.dto.contacts;

public record CompanyContactDetailDTO(
        String id,
        String companyName,
        String mailAddress,
        boolean kcUser
) { }