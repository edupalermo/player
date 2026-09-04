package org.palermo.totalbattle.util.bean;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class Cache {
    
    private static final Duration KEEP_ALIVE = Duration.ofMinutes(2);

    private final Map<String, CacheEntry<?>> cache = new HashMap<>();

    public <T> T get(String name, Class<T> clazz, Supplier<T> supplier) {
        CacheEntry<?> entry = cache.get(name);

        if (entry != null && !entry.isExpired()) {
            System.out.println("Got from cache: " + name);
            return clazz.cast(entry.value());
        }

        T value = supplier.get();
        cache.put(name, new CacheEntry<>(value, Instant.now()));

        return value;
    }

    private record CacheEntry<T>(T value, Instant createdAt) {
        boolean isExpired() {
            return Duration.between(createdAt, Instant.now())
                    .compareTo(KEEP_ALIVE) > 0;
        }
    }
}
