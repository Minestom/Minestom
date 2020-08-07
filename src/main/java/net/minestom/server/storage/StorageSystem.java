package net.minestom.server.storage;

/**
 * Represent a way of storing data
 * It works by using keys and values assigned to each one
 */
public interface StorageSystem {

    /**
     * Get if the folder exists
     *
     * @param folderPath the folder path
     * @return true if the folder exists
     */
    boolean exists(String folderPath);

    /**
     * Called when a folder is opened with this StorageSystem
     *
     * @param folderPath     the name of the folder
     * @param storageOptions the storage option
     */
    void open(String folderPath, StorageOptions storageOptions);

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
     * Called when the folder is closed, generally during server shutdown
     */
    void close();

}
