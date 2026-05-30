package net.minestom.server.entity.metadata.cube;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class SulfurCubeMeta extends AbstractCubeMeta {
    public SulfurCubeMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    @Override
    protected MetadataDef.Entry<Integer> sizeComponent() {
        return MetadataDef.SulfurCube.SIZE;
    }

    public boolean getFromBucket() {
        return metadata.get(MetadataDef.SulfurCube.FROM_BUCKET);
    }

    public void setFromBucket(boolean value) {
        metadata.set(MetadataDef.SulfurCube.FROM_BUCKET, value);
    }

    public int getMaxFuse() {
        return metadata.get(MetadataDef.SulfurCube.MAX_FUSE);
    }

    public void setMaxFuse(int value) {
        metadata.set(MetadataDef.SulfurCube.MAX_FUSE, value);
    }
}
