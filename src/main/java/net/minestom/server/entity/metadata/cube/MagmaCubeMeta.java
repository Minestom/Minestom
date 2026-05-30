package net.minestom.server.entity.metadata.cube;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class MagmaCubeMeta extends AbstractCubeMeta {
    public MagmaCubeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    protected MetadataDef.Entry<Integer> sizeComponent() {
        return MetadataDef.MagmaCube.SIZE;
    }
}
