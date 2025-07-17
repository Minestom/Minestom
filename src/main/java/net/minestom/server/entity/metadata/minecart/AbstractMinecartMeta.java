package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;
import net.minestom.server.entity.metadata.ObjectDataProvider;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMinecartMeta extends AbstractVehicleMeta implements ObjectDataProvider {
    protected AbstractMinecartMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getCustomBlockIdAndDamage() {
        return metadata.get(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_ID_AND_DAMAGE);
    }

    public void setCustomBlockIdAndDamage(int value) {
        metadata.set(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_ID_AND_DAMAGE, value);
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return metadata.get(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_Y_POSITION);
    }

    public void setCustomBlockYPosition(int value) {
        metadata.set(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_Y_POSITION, value);
    }

    public boolean getShowCustomBlock() {
        return metadata.get(MetadataDef.AbstractMinecart.SHOW_CUSTOM_BLOCK);
    }

    public void setShowCustomBlock(boolean show) {
        metadata.set(MetadataDef.AbstractMinecart.SHOW_CUSTOM_BLOCK, show);
    }

    @Override
    public boolean requiresVelocityPacketAtSpawn() {
        return true;
    }

}
