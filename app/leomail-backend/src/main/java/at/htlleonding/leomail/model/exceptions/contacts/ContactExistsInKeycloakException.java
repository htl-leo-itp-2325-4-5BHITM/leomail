package at.htlleonding.leomail.model.exceptions.contacts;

public class ContactExistsInKeycloakException extends RuntimeException {
    public ContactExistsInKeycloakException(String message) {
        super(message);
    }
}
