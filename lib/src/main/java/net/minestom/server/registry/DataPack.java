package net.minestom.server.registry;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record DataPack(@NotNull NamespaceID namespaceId, boolean isSynced) {
    public static final DataPack MINECRAFT_CORE = new DataPack("minecraft:core", true);

    public DataPack(@NotNull String namespaceId, boolean isSynced) {
        this(NamespaceID.from(namespaceId), isSynced);
    }
}
