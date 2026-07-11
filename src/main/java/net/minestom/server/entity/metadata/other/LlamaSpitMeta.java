package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.Nullable;

public class LlamaSpitMeta extends EntityMeta implements ObjectDataProvider {
    public LlamaSpitMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    public int getObjectData() {
        return 0;
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
