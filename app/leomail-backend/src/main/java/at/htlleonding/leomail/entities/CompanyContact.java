package at.htlleonding.leomail.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class CompanyContact extends Contact {

    @Column(nullable = false)
    public String companyName;

    @Column(nullable = false, unique = true)
    public String mailAddress;

    public CompanyContact() {
        super();
    }

    @Override
    public String getMailAddress() {
        return mailAddress;
    }
}