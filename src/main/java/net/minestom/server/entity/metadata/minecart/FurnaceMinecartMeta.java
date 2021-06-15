package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class FurnaceMinecartMeta extends AbstractMinecartMeta {
    public static final byte OFFSET = AbstractMinecartMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 1;

    public FurnaceMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    public boolean isHasFuel() {
        return super.metadata.getIndex(OFFSET, false);
    }

    public void setHasFuel(boolean value) {
        super.metadata.setIndex(OFFSET, Metadata.Boolean(value));
    }

    @Override
    public int getObjectData() {
        return 2;
    }

}
