package org.palermo.totalbattle.service.property;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class Converter {

    private static final DateTimeFormatter LDT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    public String toString(Object value) {
        if (value == null) { 
            return null; 
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof Long longValue) {
            return longValue.toString();
        }
        if (value instanceof Integer integerValue) {
            return integerValue.toString();
        }
        if (value instanceof Boolean boolValue) {
            return boolValue.toString();
        }
        if (value instanceof LocalDateTime localDateTimeValue) {
            return LDT_FORMAT.format(localDateTimeValue);
        }
        throw new RuntimeException(String.format("Type %s not implemented yet", value.getClass().getName()));
    }

    public <T> T to(String stringValue, Class<T> type) {
        if (stringValue == null) {
            return null;
        }
        if (type == String.class) {
            return type.cast(stringValue);
        }
        if (type == Long.class) {
            return type.cast(Long.parseLong(stringValue));
        }
        if (type == Integer.class) {
            return type.cast(Integer.parseInt(stringValue));
        }
        if (type == Boolean.class) {
            return type.cast(Boolean.parseBoolean(stringValue));
        }
        if (type == LocalDateTime.class) {
            return type.cast(LDT_FORMAT.parse(stringValue));
        }
        throw new RuntimeException(String.format("Type %s not implemented yet", type.getName()));
    }
}
