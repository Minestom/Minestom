package net.minestom.server.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StorageManager {

    private Supplier<StorageSystem> storageSystemSupplier = null;

    private Map<String, StorageFolder> folderMap = new HashMap<>();

    public StorageFolder getFolder(String folderName) {
        if (storageSystemSupplier == null)
            throw new NullPointerException("You need to define a storage system before using the Storage API");
        
        StorageSystem storageSystem = storageSystemSupplier.get();
        return folderMap.computeIfAbsent(folderName, s -> new StorageFolder(storageSystem, folderName));
    }

    public Collection<StorageFolder> getLoadedFolders() {
        return Collections.unmodifiableCollection(folderMap.values());
    }

    public void defineStorageSystem(Supplier<StorageSystem> storageSystemSupplier) {
        if (this.storageSystemSupplier != null)
            System.out.println("WARNING: the current StorageSystem is being changed, could lead to issue!");
        this.storageSystemSupplier = storageSystemSupplier;
    }
}
