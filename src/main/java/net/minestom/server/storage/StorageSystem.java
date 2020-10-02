package net.minestom.server.storage;

/**
 * Represent a way of storing data by key/value.
 * The location does not have to be a file or folder path. It is the 'identifier' of the data location
 */
public interface StorageSystem {

    /**
     * Get if the location exists
     *
     * @param location the location
     * @return true if the location exists
     */
    boolean exists(String location);

    /**
     * Called when a {@link StorageLocation} is opened with this {@link StorageSystem}
     *
     * @param location       the location name
     * @param storageOptions the {@link StorageOptions}
     */
    void open(String location, StorageOptions storageOptions);

    /**
     * Get the data associated to a key
     *
     * @param key the key to retrieve
     * @return the retrieved data
     */
    byte[] get(String key);

    /**
     * Set the specified data to the defined key
     *
     * @param key  the key of the data
     * @param data the data
     */
    void set(String key, byte[] data);

    /**
     * Delete the specified key from the database
     *
     * @param key the key to delete
     */
    void delete(String key);

    /**
     * Called when the location is closed, generally during server shutdown
     */
    void close();

}
