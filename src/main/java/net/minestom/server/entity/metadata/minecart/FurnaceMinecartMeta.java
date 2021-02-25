package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class FurnaceMinecartMeta extends AbstractMinecartMeta {

    public FurnaceMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHasFuel() {
        return super.metadata.getIndex((byte) 13, false);
    }

    public void setHasFuel(boolean value) {
        super.metadata.setIndex((byte) 13, Metadata.Boolean(value));
    }

    @Override
    public int getObjectData() {
        return 2;
    }

}
