package net.minestom.server.entity.metadata.other;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import net.minestom.server.utils.Direction;
import org.jetbrains.annotations.Nullable;

public sealed abstract class HangingMeta extends EntityMeta implements ObjectDataProvider permits ItemFrameMeta, PaintingMeta {

    protected HangingMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public Direction getDirection() {
        return get(MetadataDef.Hanging.DIRECTION);
    }

    public void setDirection(Direction direction) {
        set(MetadataDef.Hanging.DIRECTION, direction);
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
