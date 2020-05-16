package net.minestom.server.storage;

public interface StorageSystem {

    /**
     * Called when a foler is opened with this StorageSystem
     *
     * @param folderName the name of the folder
     */
    void open(String folderName);

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
