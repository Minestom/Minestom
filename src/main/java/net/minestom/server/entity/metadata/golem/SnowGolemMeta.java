package net.minestom.server.entity.metadata.golem;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class SnowGolemMeta extends AbstractGolemMeta {
    public SnowGolemMeta(@Nullable Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isHasPumpkinHat() {
        return metadata.get(MetadataDef.SnowGolem.PUMPKIN_HAT);
    }

    public void setHasPumpkinHat(boolean value) {
        metadata.set(MetadataDef.SnowGolem.PUMPKIN_HAT, value);
    }

}
