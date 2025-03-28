package at.htlleonding.leomail.model.exceptions.contacts;

public class ContactInUseException extends RuntimeException {
    public ContactInUseException(String message) {
        super(message);
    }
}