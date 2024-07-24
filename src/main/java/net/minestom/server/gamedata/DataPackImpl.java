package net.minestom.server.gamedata;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;

record DataPackImpl(@NotNull Key namespaceId, boolean isSynced) implements DataPack {

    @Override
    public boolean isSynced() {
        return false;
    }
}
