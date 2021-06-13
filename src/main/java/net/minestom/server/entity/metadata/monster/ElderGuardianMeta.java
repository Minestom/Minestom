package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Metadata;
import org.jetbrains.annotations.NotNull;

public class ElderGuardianMeta extends GuardianMeta {
    public static final byte OFFSET = GuardianMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    public ElderGuardianMeta(@NotNull Entity entity, @NotNull Metadata metadata) {
        super(entity, metadata);
    }

}
