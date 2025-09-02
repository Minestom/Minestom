package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public class BasePiglinMeta extends MonsterMeta {
    protected BasePiglinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return metadata.get(MetadataDef.BasePiglin.IMMUNE_ZOMBIFICATION);
    }

    public void setImmuneToZombification(boolean value) {
        metadata.set(MetadataDef.BasePiglin.IMMUNE_ZOMBIFICATION, value);
    }

}
