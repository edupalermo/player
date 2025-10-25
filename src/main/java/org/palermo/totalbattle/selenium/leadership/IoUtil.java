package org.palermo.totalbattle.selenium.leadership;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public class IoUtil {

    public static void serializeToFile(String filename, Object object) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(object);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Optional<T> deserializeFromFile(String filename, Class<T> type) {
        File file = new File(filename);
        if (!file.exists()) {
            return Optional.empty();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = ois.readObject();
            return Optional.of(type.cast(obj));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
