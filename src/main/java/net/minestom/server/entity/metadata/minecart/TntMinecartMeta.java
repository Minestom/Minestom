package net.minestom.server.entity.metadata.minecart;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class TntMinecartMeta extends AbstractMinecartMeta {
    public static final byte OFFSET = AbstractMinecartMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public TntMinecartMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

    @Override
    public int getObjectData() {
        return 3;
    }

}
