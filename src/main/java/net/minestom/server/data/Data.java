package net.minestom.server.data;

import net.minestom.server.MinecraftServer;
import net.minestom.server.utils.clone.PublicCloneable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

/**
 * Represents an object which contains key/value based data.
 * <p>
 * See {@link DataImpl} for the default implementation.
 */
@Deprecated
public abstract class Data implements PublicCloneable<Data> {

    public static final Data EMPTY = new Data() {
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
     * @param type  the value type, {@link #set(String, Object)} can be used instead.
     *              null if {@code value} is also null
     * @param <T>   the value generic
     */
    public abstract <T> void set(@NotNull String key, @Nullable T value, @Nullable Class<T> type);

    /**
     * Assigns a value to a specific key.
     * <p>
     * Will by default call {@link #set(String, Object, Class)} with the type sets to {@link T#getClass()}.
     *
     * @param key   the key
     * @param value the value object, null to remove the key
     * @param <T>   the value generic
     */
    public <T> void set(@NotNull String key, @Nullable T value) {
        set(key, value, value != null ? (Class<T>) value.getClass() : null);
    }

    /**
     * Retrieves a value based on its key.
     *
     * @param key the key
     * @param <T> the value type
     * @return the data associated with the key or null
     */
    @Nullable
    public abstract <T> T get(@NotNull String key);

    /**
     * Retrieves a value based on its key, give a default value if not found.
     *
     * @param key          the key
     * @param defaultValue the value to return if the key is not found
     * @param <T>          the value type
     * @return {@link #get(String)} if found, {@code defaultValue} otherwise
     */
    @Nullable
    public abstract <T> T getOrDefault(@NotNull String key, @Nullable T defaultValue);

    /**
     * Gets if the data has a key.
     *
     * @param key the key to check
     * @return true if the data contains the key
     */
    public abstract boolean hasKey(@NotNull String key);

    /**
     * Gets the list of data keys.
     *
     * @return an unmodifiable {@link Set} containing all the keys
     */
    @NotNull
    public abstract Set<String> getKeys();

    /**
     * Gets if the data is empty or not.
     *
     * @return true if the data does not contain anything, false otherwise
     */
    public abstract boolean isEmpty();

    @NotNull
    @Override
    public Data clone() {
        try {
            return (Data) super.clone();
        } catch (CloneNotSupportedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
            throw new IllegalStateException("Weird thing happened");
        }
    }
}
