package net.minestom.server.entity.metadata.monster.raider;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;

public sealed abstract class AbstractIllagerMeta extends RaiderMeta permits PillagerMeta, SpellcasterIllagerMeta, VindicatorMeta {
    protected AbstractIllagerMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
