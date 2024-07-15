package net.minestom.server.entity.metadata.monster.skeleton;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.monster.MonsterMeta;
import org.jetbrains.annotations.NotNull;

public class AbstractSkeletonMeta extends MonsterMeta {
    public static final byte OFFSET = MonsterMeta.MAX_OFFSET;
    public static final byte MAX_OFFSET = OFFSET + 0;

    protected AbstractSkeletonMeta(@NotNull Entity entity, @NotNull MetadataHolder metadata) {
        super(entity, metadata);
    }

}
