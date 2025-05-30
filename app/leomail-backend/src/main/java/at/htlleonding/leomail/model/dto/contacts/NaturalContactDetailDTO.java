package at.htlleonding.leomail.model.dto.contacts;

import at.htlleonding.leomail.model.enums.Gender;

public record NaturalContactDetailDTO(
        String id,
        String firstName,
        String lastName,
        String mailAddress,
        String prefixTitle,
        String suffixTitle,
        String company,
        String positionAtCompany,
        Gender gender,
        boolean kcUser
) { }
