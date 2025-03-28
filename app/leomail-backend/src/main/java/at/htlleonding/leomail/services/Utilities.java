package at.htlleonding.leomail.services;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Utilities {

    /*
     * This method returns a list of all fields of an object that are null.
     * @param obj The object to check for null fields
     * @return A list of all fields that are null
     * @return null if the object is null
     * @return An empty list if no fields are null
     */

    public static List<String> listNullFields(Object obj) {
        if (obj == null) {
            return null;
        }

        List<String> result = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                if (field.get(obj) == null) {
                    result.add(field.getName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return result;
    }

    public static List<String> listNullFields(Object obj, List<String> allowed) {
        if (obj == null) {
            return null;
        }

        allowed = allowed.stream().map(String::toLowerCase).toList();

        List<String> result = new ArrayList<>();
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            if(!allowed.contains(field.getName().toLowerCase())) {
                try {
                    if (field.get(obj) == null) {
                        result.add(field.getName());
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }

    public static Object setEmptyStringsToNull(Object obj) {
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                if (field.get(obj) != null && field.get(obj).equals("")) {
                    field.set(obj, null);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }
}
