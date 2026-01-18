package net.minestom.server.entity.metadata.flying;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class PhantomMeta extends FlyingMeta {
    public PhantomMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getSize() {
        return get(MetadataDef.Phantom.SIZE);
    }

    public void setSize(int value) {
        set(MetadataDef.Phantom.SIZE, value);
    }

}
