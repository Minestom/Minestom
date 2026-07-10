package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.MetaTarget;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import org.jetbrains.annotations.Nullable;

public class WardenMeta extends MonsterMeta {
    public WardenMeta(@Nullable MetaTarget entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getAngerLevel() {
        return metadata.get(MetadataDef.Warden.ANGER_LEVEL);
    }

    public void setAngerLevel(int value) {
        metadata.set(MetadataDef.Warden.ANGER_LEVEL, value);
    }

}
