package net.minestom.server.storage;

import net.minestom.server.utils.validate.Check;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StorageManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageManager.class);

    private Supplier<StorageSystem> defaultStorageSystemSupplier = null;

    // Folder path -> storage folder object
    private Map<String, StorageFolder> folderMap = new HashMap<>();

    /**
     * Used to get an access to the specified folder
     * WARNING: a storage folder needs to be created with an unique storage system linked
     * you cannot open the save folder with two or more different StorageSystem implementation
     *
     * @param folderPath     the path to the folder
     * @param storageOptions the storage option
     * @param storageSystem  the storage system used in the specified folder
     * @return the specified storage folder
     */
    public StorageFolder getFolder(String folderPath, StorageOptions storageOptions, StorageSystem storageSystem) {
        Check.notNull(storageOptions, "The storage option cannot be null");
        StorageFolder storageFolder =
                folderMap.computeIfAbsent(folderPath, s -> new StorageFolder(storageSystem, folderPath, storageOptions));
        return storageFolder;
    }

    /**
     * Used to get an access to the specified folder
     * The default StorageSystem provider will be used
     *
     * @param folderPath     the path to the folder
     * @param storageOptions the storage option
     * @return the specified storage default with the default
     * @throws NullPointerException if no default StorageSystem is defined {@link #defineDefaultStorageSystem(Supplier)}
     */
    public StorageFolder getFolder(String folderPath, StorageOptions storageOptions) {
        Check.notNull(defaultStorageSystemSupplier,
                "You need to either define a default storage system or specify your storage system for this specific folder");
        StorageSystem storageSystem = defaultStorageSystemSupplier.get();
        return getFolder(folderPath, storageOptions, storageSystem);
    }

    /**
     * Used to get an access to the specified folder
     * The default StorageSystem provider will be used
     *
     * @param folderPath the path to the folder
     * @return the specified storage default with the default
     * @throws NullPointerException if no default StorageSystem is defined {@link #defineDefaultStorageSystem(Supplier)}
     */
    public StorageFolder getFolder(String folderPath) {
        return getFolder(folderPath, new StorageOptions());
    }

    /**
     * Used to know if the specified folder already exist or not
     *
     * @param folderPath
     * @param storageSystem
     * @return true if the folder exists, false otherwise
     */
    public boolean folderExists(String folderPath, StorageSystem storageSystem) {
        return storageSystem.exists(folderPath);
    }

    /**
     * Call {@link #folderExists(String, StorageSystem)} with the default StorageSystem
     *
     * @param folderPath
     * @return
     */
    public boolean folderExists(String folderPath) {
        return folderExists(folderPath, defaultStorageSystemSupplier.get());
    }

    /**
     * Get all folders which have been loaded by {@link #getFolder(String)} or {@link #getFolder(String, StorageOptions, StorageSystem)}
     *
     * @return an unmodifiable list of all the loaded StorageFolder
     */
    public Collection<StorageFolder> getLoadedFolders() {
        return Collections.unmodifiableCollection(folderMap.values());
    }

    public void defineDefaultStorageSystem(Supplier<StorageSystem> storageSystemSupplier) {
        if (this.defaultStorageSystemSupplier != null) {
            LOGGER.warn("The default storage-system has been changed. This could lead to issues!");
        }
        this.defaultStorageSystemSupplier = storageSystemSupplier;
    }

    public boolean isDefaultStorageSystemDefined() {
        return defaultStorageSystemSupplier != null;
    }
}
