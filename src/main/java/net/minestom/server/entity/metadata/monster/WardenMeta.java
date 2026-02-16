package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public final class WardenMeta extends MonsterMeta {
    public WardenMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public int getAngerLevel() {
        return get(MetadataDef.Warden.ANGER_LEVEL);
    }

    public void setAngerLevel(int value) {
        set(MetadataDef.Warden.ANGER_LEVEL, value);
    }

}
