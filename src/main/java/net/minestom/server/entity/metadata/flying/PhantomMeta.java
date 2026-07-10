package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class PhantomMeta extends FlyingMeta {
    public PhantomMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getSize() {
        return metadata.get(MetadataDef.Phantom.SIZE);
    }

    public void setSize(int value) {
        metadata.set(MetadataDef.Phantom.SIZE, value);
    }

}
