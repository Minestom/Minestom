package net.minestom.server.entity.metadata.monster;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataDef;
import net.minestom.server.entity.MetadataHolder;

public sealed abstract class BasePiglinMeta extends MonsterMeta permits PiglinBruteMeta, PiglinMeta {
    protected BasePiglinMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

    public boolean isImmuneToZombification() {
        return get(MetadataDef.BasePiglin.IMMUNE_ZOMBIFICATION);
    }

    public void setImmuneToZombification(boolean value) {
        set(MetadataDef.BasePiglin.IMMUNE_ZOMBIFICATION, value);
    }

}
