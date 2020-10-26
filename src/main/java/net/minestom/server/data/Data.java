package net.minestom.server.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Represents an object which contains key/value based data.
 * <p>
 * See {@link DataImpl} for the default implementation.
 */
public interface Data {

    Data EMPTY = new Data() {
        @Override
        public <T> void set(@NotNull String key, @Nullable T value, @Nullable Class<T> type) {
        }

        @Override
        public <T> T get(@NotNull String key) {
            return null;
        }

        @Override
        public boolean hasKey(@NotNull String key) {
            return false;
        }

        @NotNull
        @Override
        public Set<String> getKeys() {
            return Collections.emptySet();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @NotNull
        @Override
        public Data clone() {
            return this;
        }

        @Override
        public <T> T getOrDefault(@NotNull String key, T defaultValue) {
            return defaultValue;
        }
    };

    /**
     * Assigns a value to a specific key.
     *
     * @param key   the key
     * @param value the value object, null to remove the key
     * @param type  the value type, can be null if not in a {@link SerializableData}
     * @param <T>   the value generic
     */
    <T> void set(@NotNull String key, @Nullable T value, @Nullable Class<T> type);

    /**
     * Retrieves a value based on its key.
     *
     * @param key the key
     * @param <T> the value type
     * @return the data associated with the key or null
     */
    @Nullable
    <T> T get(@NotNull String key);

    /**
     * Retrieves a value based on its key, give a default value if not found.
     *
     * @param key          the key
     * @param defaultValue the value to return if the key is not found
     * @param <T>          the value type
     * @return {@link #get(String)} if found, {@code defaultValue} otherwise
     */
    @Nullable
    <T> T getOrDefault(@NotNull String key, @Nullable T defaultValue);

    /**
     * Gets if the data has a key.
     *
     * @param key the key to check
     * @return true if the data contains the key
     */
    boolean hasKey(@NotNull String key);

    /**
     * Gets the list of data keys.
     *
     * @return an unmodifiable {@link Set} containing all the keys
     */
    @NotNull
    Set<String> getKeys();

    /**
     * Gets if the data is empty or not.
     *
     * @return true if the data does not contain anything, false otherwise
     */
    boolean isEmpty();

    /**
     * Clones this data.
     *
     * @return a cloned data object
     */
    @NotNull
    Data clone();

}
