package net.minestom.server.entity.metadata.projectile;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.NotNull;

public class WindChargeMeta extends AbstractWindChargeMeta {
    public static final byte OFFSET = AbstractWindChargeMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public WindChargeMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
