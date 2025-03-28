package at.htlleonding.leomail.entities;

import jakarta.persistence.*;
import at.htlleonding.leomail.model.enums.Gender;

@Entity
public class NaturalContact extends Contact {

    @Column(nullable = false)
    public String firstName;

    @Column(nullable = false)
    public String lastName;

    public String prefixTitle;

    public String suffixTitle;

    public String company; // Optional

    public String positionAtCompany; // Optional

    @Enumerated(EnumType.STRING)
    public Gender gender;

    @Column(nullable = false, unique = true)
    public String mailAddress;

    @Column(name = "outlook_encrypted_password")
    public String encryptedOutlookPassword;

    public NaturalContact() {
        super();
    }

    @Override
    public String getMailAddress() {
        return mailAddress;
    }
}