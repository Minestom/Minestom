package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.AbstractVehicleMeta;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractMinecartMeta extends AbstractVehicleMeta {
    protected AbstractMinecartMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public @Nullable Block getCustomBlockState() {
        return metadata.get(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_STATE);
    }

    public void setCustomBlockState(@Nullable Block value) {
        metadata.set(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_STATE, value);
    }

    // in 16th of a block
    public int getCustomBlockYPosition() {
        return metadata.get(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_Y_POSITION);
    }

    public void setCustomBlockYPosition(int value) {
        metadata.set(MetadataDef.AbstractMinecart.CUSTOM_BLOCK_Y_POSITION, value);
    }

}
