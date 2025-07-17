package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HangingMeta extends EntityMeta implements ObjectDataProvider {

    protected HangingMeta(@Nullable Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @NotNull Direction getDirection() {
        return metadata.get(MetadataDef.Hanging.DIRECTION);
    }

    public void setDirection(@NotNull Direction direction) {
        metadata.set(MetadataDef.Hanging.DIRECTION, direction);
    }

    @Override
    public int getObjectData() {
        return getDirection().ordinal();
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return false;
    }
}
