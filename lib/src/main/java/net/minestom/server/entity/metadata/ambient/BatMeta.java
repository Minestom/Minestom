package net.minestom.server.entity.metadata.ambient;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class BatMeta extends AmbientCreatureMeta {
    public BatMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHanging() {
        return metadata.get(MetadataDef.Bat.IS_HANGING);
    }

    public void setHanging(boolean value) {
        metadata.set(MetadataDef.Bat.IS_HANGING, value);
    }

}
