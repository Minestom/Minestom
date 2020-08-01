package net.minestom.server.storage;

public interface StorageSystem {

    /**
     * @param folderPath
     * @return true if the folder exists, false otherwise
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
     * @param key
     * @return the retrieved data
     */
    byte[] get(String key);

    /**
     * Set the specified data to the defined key
     *
     * @param key
     * @param data
     */
    void set(String key, byte[] data);

    /**
     * Delete the specified key from the database
     *
     * @param key
     */
    void delete(String key);

    /**
     * Called when the folder is closed, generally during server shutdown
     */
    void close();

}
