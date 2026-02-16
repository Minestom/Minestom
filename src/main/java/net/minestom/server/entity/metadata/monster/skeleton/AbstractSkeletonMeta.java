package net.minestom.server.entity.metadata.monster.skeleton;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.MetadataHolder;
import net.minestom.server.entity.metadata.monster.MonsterMeta;

public sealed abstract class AbstractSkeletonMeta extends MonsterMeta permits BoggedMeta, ParchedMeta, SkeletonMeta, StrayMeta, WitherSkeletonMeta {
    protected AbstractSkeletonMeta(Entity entity, MetadataHolder metadata) {
        super(entity, metadata);
    }

}
