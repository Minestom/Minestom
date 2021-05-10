package net.minestom.server.extras.selfmodification.mixins;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Global properties service for Mixin
 */
public class GlobalPropertyServiceMinestom implements IGlobalPropertyService {

    private static class BasicProperty implements IPropertyKey {

        private final String name;

        public BasicProperty(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BasicProperty that = (BasicProperty) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return "BasicProperty{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    private final Map<String, IPropertyKey> keys = new HashMap<>();
    private final Map<IPropertyKey, Object> values = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return keys.computeIfAbsent(name, BasicProperty::new);
    }

    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T) values.get(key);
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        values.put(key, value);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) values.getOrDefault(key, defaultValue);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return (String) values.getOrDefault(key, defaultValue);
    }
}
