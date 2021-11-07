package net.minestom.server.storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a way of storing data by key/value.
 * The location does not have to be a file or folder path. It is the 'identifier' of the data location.
 */
@Deprecated
public interface StorageSystem {

    /**
     * Gets if the location exists.
     *
     * @param location the location
     * @return true if the location exists
     */
    boolean exists(@NotNull String location);

    /**
     * Called when a {@link StorageLocation} is opened with this {@link StorageSystem}.
     *
     * @param location       the location name
     * @param storageOptions the {@link StorageOptions}
     */
    void open(@NotNull String location, @NotNull StorageOptions storageOptions);

    /**
     * Gets the data associated to a key.
     *
     * @param key the key to retrieve
     * @return the retrieved data, null if the data doesn't exist
     */
    @Nullable
    byte[] get(@NotNull String key);

    /**
     * Sets the specified data to the defined key.
     *
     * @param key  the key of the data
     * @param data the data
     */
    void set(@NotNull String key, byte[] data);

    /**
     * Deletes the specified key from the database.
     *
     * @param key the key to delete
     */
    void delete(@NotNull String key);

    /**
     * Called when the location is closed, generally during server shutdown.
     */
    void close();

}
