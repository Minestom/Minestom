package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.monster.MonsterMeta;

public class RaiderMeta extends MonsterMeta {
    protected RaiderMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isCelebrating() {
        return metadata.get(MetadataDef.Raider.IS_CELEBRATING);
    }

    public void setCelebrating(boolean value) {
        metadata.set(MetadataDef.Raider.IS_CELEBRATING, value);
    }

}
