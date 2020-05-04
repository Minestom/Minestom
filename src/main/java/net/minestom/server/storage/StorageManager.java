package net.minestom.server.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class StorageManager {

    private static Supplier<StorageSystem> storageSystemSupplier = null;

    private static Map<String, StorageFolder> folderMap = new HashMap<>();

    public static StorageFolder getFolder(String folderName) {
        StorageSystem storageSystem = storageSystemSupplier.get();
        return folderMap.computeIfAbsent(folderName, s -> new StorageFolder(storageSystem, folderName));
    }

    public static Collection<StorageFolder> getLoadedFolders() {
        return Collections.unmodifiableCollection(folderMap.values());
    }

    public static void defineStorageSystem(Supplier<StorageSystem> storageSystemSupplier) {
        if (StorageManager.storageSystemSupplier != null)
            System.out.println("WARNING: the current StorageSystem is being changed, could lead to issue!");
        StorageManager.storageSystemSupplier = storageSystemSupplier;
    }
}
