package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;
import net.minestom.server.entity.metadata.EntityMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMinecartMeta extends AbstractVehicleMeta implements ObjectDataProvider {
    public static final byte OFFSET = AbstractVehicleMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 3;

    protected AbstractMinecartMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getCustomBlockIdAndDamage() {
        return super.metadata.getIndex(OFFSET, 0);
    }

    public void setCustomBlockIdAndDamage(int value) {
        super.metadata.setIndex(OFFSET, Metadata.VarInt(value));
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return super.metadata.getIndex(OFFSET + 1, 6);
    }

    public void setCustomBlockYPosition(int value) {
        super.metadata.setIndex(OFFSET + 1, Metadata.VarInt(value));
    }

    public boolean getShowCustomBlock() {
        return super.metadata.getIndex(OFFSET + 2, false);
    }

    public void setShowCustomBlock(boolean show) {
        super.metadata.setIndex(OFFSET + 2, Metadata.Boolean(show));
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
