package at.htlleonding.leomail.model.exceptions;

import java.util.List;

public class ObjectContainsNullAttributesException extends RuntimeException {
    private List<String> nullFields;

    public ObjectContainsNullAttributesException(List<String> nullFields) {
        super();
        this.nullFields = nullFields;
    }

    public ObjectContainsNullAttributesException() {
        super();
    }

    public List<String> getFields() {
        return this.nullFields;
    }
}
