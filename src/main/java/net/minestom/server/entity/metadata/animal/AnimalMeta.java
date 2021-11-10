package net.minestom.server.entity.metadata.animal;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import net.minestom.server.entity.metadata.AgeableMobMeta;
import org.jetbrains.annotations.NotNull;

public class AnimalMeta extends AgeableMobMeta {
    public static final byte OFFSET = AgeableMobMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    protected AnimalMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
