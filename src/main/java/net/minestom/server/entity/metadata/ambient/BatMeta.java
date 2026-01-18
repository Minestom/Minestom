package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class BatMeta extends AmbientCreatureMeta {
    public BatMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHanging() {
        return get(MetadataDef.Bat.IS_HANGING);
    }

    public void setHanging(boolean value) {
        set(MetadataDef.Bat.IS_HANGING, value);
    }

}
