package at.htlleonding.leomail.model;

import java.io.Serializable;
import java.util.List;

public record Receiver(List<String> contacts,
                       List<String> groups) implements Serializable {
    public Receiver {
        if (contacts == null) {
            contacts = List.of();
        }
        if (groups == null) {
            groups = List.of();
        }
    }
}
