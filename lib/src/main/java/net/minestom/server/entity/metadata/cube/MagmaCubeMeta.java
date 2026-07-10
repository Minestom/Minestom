package net.minestom.server.entity.metadata.cube;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public final class MagmaCubeMeta extends AbstractCubeMeta {
    public MagmaCubeMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    protected MetadataDef.Entry<Integer> sizeComponent() {
        return MetadataDef.MagmaCube.SIZE;
    }
}
