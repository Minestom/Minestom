package net.minestom.server.entity.metadata.villager;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class WanderingTraderMeta extends VillagerMeta {
    public static final byte OFFSET = VillagerMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public WanderingTraderMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
