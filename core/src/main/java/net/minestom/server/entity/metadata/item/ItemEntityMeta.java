package net.minestom.server.entity.metadata.item;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

public class ItemEntityMeta extends ItemContainingMeta implements ObjectDataProvider {

    public ItemEntityMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata, Material.AIR);
    }

    @Override
    public int getObjectData() {
        return 1;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
