package net.minestom.server.gamedata;

import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

record DataPackImpl(@NotNull NamespaceID namespaceId, boolean isSynced) implements DataPack {

    @Override
    public boolean isSynced() {
        return false;
    }
}
