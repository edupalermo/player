package org.palermo.totalbattle.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.palermo.totalbattle.player.bean.ArmyBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IoUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // write ISO strings, not timestamps
    }

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

    public static <T> T readJson(File file, Class<T> valueType) {
        if (file.exists()) {
            try {
                return mapper.readValue(file, valueType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            try {
                return valueType.getDeclaredConstructor().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void writeJson(File file, Object object) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
