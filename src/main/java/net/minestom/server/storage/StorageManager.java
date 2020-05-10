package net.minestom.server.storage;

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
     * @param folderPath    the path to the folder
     * @param storageSystem the storage system used in the specified folder
     * @return the specified storage folder
     */
    public StorageFolder getFolder(String folderPath, StorageSystem storageSystem) {
        return folderMap.computeIfAbsent(folderPath, s -> new StorageFolder(storageSystem, folderPath));
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
        if (defaultStorageSystemSupplier == null)
            throw new NullPointerException("You need to either define a default storage system or specify your storage system for this specific folder");

        StorageSystem storageSystem = defaultStorageSystemSupplier.get();
        return getFolder(folderPath, storageSystem);
    }

    public Collection<StorageFolder> getLoadedFolders() {
        return Collections.unmodifiableCollection(folderMap.values());
    }

    public void defineDefaultStorageSystem(Supplier<StorageSystem> storageSystemSupplier) {
        if (this.defaultStorageSystemSupplier != null) {
            LOGGER.error("The default storage-system has been changed. This could lead to issues!");
        }
        this.defaultStorageSystemSupplier = storageSystemSupplier;
    }
}
