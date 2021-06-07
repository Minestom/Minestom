package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class HopperMinecartMeta extends AbstractMinecartContainerMeta {
    public static final byte OFFSET = AbstractMinecartContainerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public HopperMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Override
    public int getObjectData() {
        return 5;
    }

}
